#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
This script takes a WMT-formatted CSV file and displays all the systems that have been ranked for
each sentence, in descending order of their ranks on that sentence. The ranks are computed by
examining all pairwise comparisons for the sentence and determining what percent of the time a
system was ranked as best.

"""

import re
import os
import sys
import argparse
from collections import defaultdict
from csv import DictReader
from itertools import combinations

if not os.environ.has_key('WMT13'):
    print >> sys.stderr, "FATAL: Please set the environment variable WMT13 to the path to the WMT13 raw data"
    print >> sys.stderr, "  You can download this from statmt.org/wmt13/wmt13-data.tar.gz"
    print >> sys.stderr, "  See the README file for more information"
    sys.exit(1)

PARSER = argparse.ArgumentParser(description="Visualize ranked system outputs.")
PARSER.add_argument('-path', type=str, default=os.environ['WMT13'], help='path to system outputs')
PARSER.add_argument('-judge', type=str, default='', help='judge type (default all)')
PARSER.add_argument('wmt', type=str, default=None, help='WMT-formatted file containing judgments')
PARSER.add_argument('langpair', type=str, default=None, help='language pair to display')

def read_file(filename, list):
    """Read in a file to an array."""
    for line in open(filename):
        list.append(line.rstrip())

def normalize(system):
    """Takes a system name and removes redundant portions of the name, e.g., newstest2013.cs-en.JHU.2903 -> JHU"""
    return re.sub('.\d+$', '',system).split('.')[-1]

def increment_rankings(row, COUNTS, TOTALS):
    """Takes a DictReader row and computes all the rankings, incrementing counts for each system."""

    sentno = int(row.get('srcIndex'))

    for pair in combinations(range(1,6),2):
        rank1 = int(row.get('system%drank' % (pair[0])))
        rank2 = int(row.get('system%drank' % (pair[1])))
        sys1 = normalize(row.get('system%dId' % (pair[0])))
        sys2 = normalize(row.get('system%dId' % (pair[1])))

        for sys in [sys1, sys2]:
            if not COUNTS[sentno].has_key(sys):
                COUNTS[sentno][sys] = 0
            if not TOTALS[sentno].has_key(sys):
                TOTALS[sentno][sys] = 0

        if rank1 != rank2:
            if rank1 < rank2:
                COUNTS[sentno][sys1] += 1
            elif rank2 < rank1:
                COUNTS[sentno][sys2] += 1

        TOTALS[sentno][sys1] += 1
        TOTALS[sentno][sys2] += 1

if __name__ == "__main__":
    args = PARSER.parse_args()

    LANGS = { 'Czech': 'cs',
              'Russian': 'ru',
              'German': 'de',
              'Spanish': 'es',
              'English': 'en',
              'French': 'fr',
              'cs': 'cs',
              'ru': 'ru',
              'de': 'de',
              'es': 'es',
              'en': 'en',
              'fr': 'fr' }

    # Read source, reference, and system sentences
    sources = defaultdict(dict)
    refs = {}
    systems = {}
    source,target = args.langpair.split('-')
    sources = []
    refs = []
    systems = defaultdict(list)
    read_file('%s/plain/sources/newstest2013-src.%s' % (args.path, source), sources)
    read_file('%s/plain/references/newstest2013-ref.%s' % (args.path, target), refs)
    system_dir = os.path.join(args.path,'plain/system-outputs/newstest2013', args.langpair)
    for system in os.listdir(system_dir):
        system_name = normalize(system)
        if system.startswith('newstest2013.%s' % (args.langpair)):
            read_file(os.path.join(system_dir, system), systems[system_name])

    # Read in the controls
    COUNTS = defaultdict(defaultdict)
    TOTALS = defaultdict(defaultdict)
    for row in DictReader(open(args.wmt)):
        if row.get('srcIndex') is None:
            print >> sys.stderr, 'bad line', row
            continue
        sentno = int(row.get('srcIndex'))
        langpair = '%s-%s' % (LANGS[row.get('srclang')], LANGS[row.get('trglang')])
        if row.get('judgeId').startswith(args.judge):
            if langpair == args.langpair:
                increment_rankings(row, COUNTS, TOTALS)

    # Read through all the source sentences
    for srcIndex, line in enumerate(sources, start = 1):
        if not COUNTS.has_key(srcIndex): 
            continue

        if sum(COUNTS[srcIndex].values()) == 0:
            continue

        print 'SENTENCE', srcIndex
        print 'SOURCE', sources[srcIndex-1]
        print 'REFERENCE', refs[srcIndex-1]

        system_list = sorted(COUNTS[srcIndex].items(), key=lambda x: x[1] * 1.0 / TOTALS[srcIndex][x[0]], reverse = True)

        for system,count in system_list:
            # print '[%02d / %02d = %.02f] %s [%s]' % (count, TOTALS[srcIndex][system], 1.0 * count / TOTALS[srcIndex][system], systems[system][srcIndex-1], system)
            print '[%.02f] %s [%s]' % (1.0 * count / TOTALS[srcIndex][system], systems[system][srcIndex-1], system)

        print

#!/usr/bin/python

# Copied from Chipyard. A little enhanced to support nested includes.
# replaces a `include with the full include file
#
# args
# $1 - file to remove includes from
# $2 - file to write output to
# $3 - list of directories to search for includes in (note: NON-RECURSIVE must specify all dirs)
#      includes are found relative to this path
#      this is equivalent to something like +incdir+

import sys
import re
import os

inVlog = sys.argv[1]
outVlog = sys.argv[2]
print("[INFO] Replaces includes from: " + str(inVlog))

if inVlog == outVlog:
    sys.exit("[ERROR] The input and output file cannot be the same.")

# add directories to search list
incDirs = sys.argv[3:]
#print("[INFO] Searching following dirs for includes: " + str(incDirs))

# open file
lineList = []
with open(inVlog, 'r') as inFile:
    for line in inFile:
        lineList.append(line)
lineListO = []
includes_found = 1
included = []
where_included = []
while includes_found != 0:
    includes_found = 0
    # for each include found, search through all dirs and replace if found, error if not
    for line in lineList:
        num = len(lineListO)
        match = re.match(r"^ *`include +\"(.*)\"", line)
        if match:
            # TODO: This is absolutelly not clean. There is a possibility that the includes gets re-included again
            # To cetainly avoid duplicate declarations, the file included should have the `pragma once definitions
            if match.group(1) in included:
                index_included = included.index(match.group(1) )
                if num < where_included[index_included]:
                    #print("[INFO] Re-including included \"" + str(match.group(1)) + "\"  found on line " + str(num))
                    where_included[index_included] = num
                    # Then continue...
                else:
                    #print("[INFO] Skipped already included \"" + str(match.group(1)) + "\" found on line " + str(num))
                    continue
            # search for include and replace
            found = False
            for d in incDirs:
                potentialIncFileName = d + "/" + match.group(1)
                if os.path.exists(potentialIncFileName):
                    found = True
                    with open(potentialIncFileName, 'r') as incFile:
                        for iline in incFile:
                            lineListO.append(iline)
                    break

            # must find something to include with
            if not found:
                print("[ERROR] Couldn't replace include \"" + str(match.group(1)) + "\" found on line " + str(num))
                lineListO.append(line)
            else:
                #print("[INFO] Replaced include \"" + str(match.group(1)) + "\" found on line " + str(num))
                includes_found = includes_found + 1
                if not match.group(1) in included:
                    included.append(match.group(1))
                    where_included.append(num)
        else:
            lineListO.append(line)
    lineList = lineListO[:]
    lineListO = []
    #print("The file is %d lines long, includes: %d" % (len(lineList), includes_found))

print("[INFO] Success. Writing output to: " + str(outVlog))
with open(outVlog, 'w') as outFile:
    for iline in lineList:
        outFile.write(iline)


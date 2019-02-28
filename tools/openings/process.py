#!/bin/python
import os
import re
from collections import OrderedDict

MOVEONLY=True
FILENAME_ALL="all_sorted.txt"
all_lines = []

def process_one_file( filename_in, filename_out ):
    filein  = open(filename_in,  "r")
    fileout = open(filename_out, "w")

    for line in filein:
        if len(line)==0 or line=="\n" or line[0]=="&": continue
        if line[0]=='C':
            #fileout.write("\n")
            firstMoves=line.replace('C ','').replace('\n','')
            firstMoves = re.sub('(\d+)\.', '\\1 .', firstMoves)
            if MOVEONLY: fileout.write(firstMoves+'\n') #only moves
            else:        fileout.write(firstMoves)  #write name
        elif line[0]!='E':
            if '~' in line: continue         #remove generic openings, ~ is a placeholder for any move
            followingMoves=re.sub('\d+\. \.\.\. ','',line)
            followingMoves = re.sub('(\d+)\.', '\\1 .', followingMoves)
            if MOVEONLY: fileout.write(firstMoves+" "+followingMoves)                     #only moves
            else:        fileout.write(firstMoves+" "+followingMoves.replace('\n','') )    #write name
        else:                           
            if not MOVEONLY: fileout.write("   "+line.replace('E ','') )                       #write name

    fileout.close()

    fileout_read = open(filename_out, "r")
    for line in fileout_read:
        all_lines.append(line)

#removes line if there is another line whose beginning is identical to this line
#do not remove if both are identical
def duplicate(line):
    for i,iline in enumerate(all_lines):
        if line.rstrip() in iline.rstrip()  and not line == iline:
            return True;
    return False;

for file in os.listdir("txt/"):
    if os.path.exists(FILENAME_ALL):
        os.remove(FILENAME_ALL)
    if file.endswith(".txt"):
        filename_in=os.path.join("txt/", file)
        process_one_file( filename_in, filename_in.replace('txt/','processed/') )

#remove actual duplicates
keeplist = list(OrderedDict.fromkeys(all_lines))

#remove openings which are extended in other openings
keeplist[:] = [l for l in all_lines if not duplicate(l)]

fileall = open(FILENAME_ALL, "w")
for line in keeplist:
    fileall.write(line)
fileall.close()
    
print len(keeplist)

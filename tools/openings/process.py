#!/bin/python
import re
import os

MOVEONLY=True
FILENAME_ALL="all_sorted.txt"
all_lines = []

def process_one_file( filename_in, filename_out ):
    filein  = open(filename_in,  "r")
    fileout = open(filename_out, "w")

    for line in filein:
        if len(line)==0 or line=="\n": continue
        if line[0]=='C':
            #fileout.write("\n")
            firstMoves=line.replace('C ','').replace('\n','')
            if MOVEONLY: fileout.write(firstMoves+'\n') #only moves
            else:        fileout.write(firstMoves)  #write name
        elif line[0]!='E':
            if '~' in line: continue         #remove generic openings, ~ is a placeholder for any move
            followingMoves=re.sub('\d+\. \.\.\. ','',line)
            if MOVEONLY: fileout.write(firstMoves+" "+followingMoves)                     #only moves
            else:        fileout.write(firstMoves+" "+followingMoves.replace('\n','') )    #write name
        else:                                                                 #write name
            if not MOVEONLY: fileout.write("   "+line.replace('E ','') )                       #write name

    fileout.close()

    fileout_read = open(filename_out, "r")
    fileall = open(FILENAME_ALL, "a")
    for line in fileout_read:
        lineExists=False
        for existing_line in all_lines:
            if line in existing_line:
                lineExists=True
                break
        if not lineExists:
            fileall.write(line)
            all_lines.append(line)
    fileall.close()


for file in os.listdir("txt/"):
    if os.path.exists(FILENAME_ALL):
        os.remove(FILENAME_ALL)
    if file.endswith(".txt"):
        filename_in=os.path.join("txt/", file)
        #print("process_one_file( "+filename_in+" "+filename_in.replace('txt/','processed/')+" )")
        process_one_file( filename_in, filename_in.replace('txt/','processed/') )

print( len(all_lines) )

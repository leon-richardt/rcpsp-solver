import matplotlib.pyplot as plt
import sys
import json
import os

import numpy as np


def addtodict (filename,prefix):
    if instancename in filename:
        with open(filename, "rb") as f:
            bench_obj = json.load(f)
            for run in bench_obj["runs"]:
                myvalues[prefix+'_iterations'].append(run["iterations"])
                makespan = -1
                for pair in run["makespan_updates"]:
                    makespan = pair["makespan"]
                if (makespan == -1):
                    raise Exception('no makespan detected')
                myvalues[prefix+'_makespan'].append(makespan)
    else:
        pass
#Einlesen der Daten aus dem Folder
myvalues={
    'one-point-0.5-0-fixed-size-false_makespan': [],
    'one-point-0.5-0-fixed-size-true_makespan': [],
    'one-point-0.5-0-random-size-false_makespan': [],
    'one-point-0.5-10-fixed-size-false_makespan': [],
    'one-point-0.5-100-fixed-size-false_makespan': [],
    'one-point-0.5-1000-fixed-size-false_makespan': [],
    'one-point-0.25-0-fixed-size-false_makespan': [],
    'one-point-0.75-0-fixed-size-false_makespan': [],
    'one-point-0-0-fixed-size-false_makespan': [],
    'one-point-1-0-fixed-size-false_makespan': [],
    'ORIGINAL_makespan': [],
    'two-point-0.5-0-fixed-size-false_makespan':[],
    'one-point-0.5-0-fixed-size-false_iterations': [],
    'one-point-0.5-0-fixed-size-true_iterations': [],
    'one-point-0.5-0-random-size-false_iterations': [],
    'one-point-0.5-10-fixed-size-false_iterations': [],
    'one-point-0.5-100-fixed-size-false_iterations': [],
    'one-point-0.5-1000-fixed-size-false_iterations': [],
    'one-point-0.25-0-fixed-size-false_iterations': [],
    'one-point-0.75-0-fixed-size-false_iterations': [],
    'one-point-0-0-fixed-size-false_iterations': [],
    'one-point-1-0-fixed-size-false_iterations': [],
    'ORIGINAL_iterations': [],
    'two-point-0.5-0-fixed-size-false_iterations': [],
}
mydirectory=''
if __name__ == "__main__":
    folder_name = sys.argv[1]
    instancename = sys.argv[2]
    #optimum = sys.argv[3]
    with os.scandir(folder_name) as entries:
        mydirectory=os.getcwd()
        os.chdir(folder_name)
        for entry in entries:
            if 'one-point-0.5-0-fixed-size-false' in entry.name:
                addtodict(entry.name,'one-point-0.5-0-fixed-size-false')
            if 'one-point-0.5-0-fixed-size-true' in entry.name:
                addtodict(entry.name,'one-point-0.5-0-fixed-size-true')
            if 'one-point-0.5-0-random-size-false' in entry.name:
                addtodict(entry.name,'one-point-0.5-0-random-size-false')
            if 'one-point-0.5-10-fixed-size-false' in entry.name:
                addtodict(entry.name,'one-point-0.5-10-fixed-size-false')
            if 'one-point-0.5-100-fixed-size-false' in entry.name:
                addtodict(entry.name,'one-point-0.5-100-fixed-size-false')
            if 'one-point-0.5-1000-fixed-size-false' in entry.name:
                addtodict(entry.name,'one-point-0.5-1000-fixed-size-false')
            if 'one-point-0.25-0-fixed-size-false' in entry.name:
                addtodict(entry.name,'one-point-0.25-0-fixed-size-false')
            if 'one-point-0.75-0-fixed-size-false' in entry.name:
                addtodict(entry.name,'one-point-0.75-0-fixed-size-false')
            if 'one-point-0-0-fixed-size-false' in entry.name:
                addtodict(entry.name,'one-point-0-0-fixed-size-false')
            if 'one-point-1-0-fixed-size-false' in entry.name:
                addtodict(entry.name,'one-point-1-0-fixed-size-false')
            if 'ORIGINAL' in entry.name:
                addtodict(entry.name,'ORIGINAL')
            if 'two-point-0.5-0-fixed-size-false' in entry.name:
                addtodict(entry.name,'two-point-0.5-0-fixed-size-false')
    plotdata_makespan = []
    plotdata_iterations = []
    beschriftung_makespan = []
    beschriftung_iterations =[]
    maxyaxis = 0
    for key in myvalues:
        if 'makespan' in key:
            plotdata_makespan.append(myvalues[key])
            beschriftung_makespan.append(key)
            if max(myvalues[key])> maxyaxis:
                maxyaxis = max(myvalues[key])
        else:
            plotdata_iterations.append(myvalues[key])
            beschriftung_iterations.append(key)
    #Try to generate a boxplot
    #move back to direction
    os.chdir(mydirectory)
    fig1, ax1 = plt.subplots()
    ax1.set_title('Makespans '+instancename)
    ax1.boxplot(plotdata_makespan)
    #print Beschriftung
    mystring = ''
    mystring = mystring+ 'Beschriftung Makespan ' +instancename+ '#######'
    zahl = 1
    for name in beschriftung_makespan:
        mystring = mystring +str(zahl)+' : '+name
        zahl= zahl+1

    #plt.ylim(round(int(optimum)-0.1*int(optimum)), maxyaxis+5)
    #plt.hlines(optimum, xmin=0, xmax=13, linestyles='dashed', colors='red')
    #plt.yticks(np.arange(round(int(optimum)-0.1*int(optimum)), maxyaxis+5,5))
    plt.savefig(fname='Makespans'+instancename+'.png',dpi=300)
    fig2, ax2 = plt.subplots()
    ax2.set_title('# Iterations ' + instancename)
    ax2.boxplot(plotdata_iterations)
    plt.savefig(fname='Iterations' + instancename + '.png', dpi=300)
    mystring2 = ''
    mystring2 = mystring2+'###### Beschriftung iterations' +instancename + '#######'+'\n'
    zahl = 1
    for name in beschriftung_iterations:
        mystring2 = mystring2+str(zahl) + ' : ' + name +'\n'
        zahl = zahl + 1
    with open('beschriftungplots'+instancename+'.txt', "w") as text_file:
        text_file.write(mystring)
        text_file.write('\n')
        text_file.write(mystring2)





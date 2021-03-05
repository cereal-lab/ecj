import os
import re
import io
import csv 
import sys
import numpy as np

params = [ "mul.n.20" ]
def cmd(p): 
    return f"cd target/classes && java ec.Evolve -file ec/app/rewrites/{p}.params"

pattern = re.compile('(?<=Standardized=)(.*?)(?=\.)')
def run(acc, cmd, n, expectedLen = 102): 
    for i in range(n):
        print(f"Running test {i}")
        os.system(cmd);
        with open("target/classes/out.stat", "r") as statFile:
            txt = statFile.read()
        res = [ int(s) for s in pattern.findall(txt) ]
        if len(res) < expectedLen: 
            res = res + [0 for i in range(expectedLen - len(res))]
        res.pop() #last is best fitness
        acc.append(res)
    return acc

def save(name, runs, mean, std, meanofmean):
    with open(name, 'w', newline='') as csvfile:
        wr = csv.writer(csvfile)
        wr.writerows(runs)
        # wr.writerow(['mean', 'std'])
        # wr.writerow(mean)
        # wr.writerow(std)
        # wr.writerow(['meanofmean'])
        # wr.writerow([meanofmean])

def read(name):
    res = []
    with open(name, 'r', newline='') as csvfile:
        rd = csv.reader(csvfile)        
        for row in rd:             
            if row[0] == 'mean':
                break 
            res.append([int(v) for v in row])
    return res; 

import scipy.stats as stats

import scikit_posthocs as sp

# def tvalue(f1, f2):
#     return stats.ttest_ind(f1, f2, equal_var=False)

# //https://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U_test
# //https://projecteuclid.org/download/pdf_1/euclid.aoms/1177730491
# //https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.mannwhitneyu.html
# def uvalue(f1, f2, alt='greater'):
    # res1 = read(f1)
    # lastGen1 = [ v[len(v) - 1] for v in res1 ]
    # res2 = read(f2)
    # lastGen2 = [ v[len(v) - 1] for v in res2 ]
    # return stats.mannwhitneyu(lastGen1, lastGen2, alternative=alt)
# //>>> uvalue("11.csv", "11-3.csv")
# //MannwhitneyuResult(statistic=1179.5, pvalue=0.6886706822413166    

# //https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.kruskal.html?highlight=kruskal
def hvalue(f1, f2):
    res1 = read(f1)
    lastGen1 = [ v[len(v) - 1] for v in res1 ]
    res2 = read(f2)
    lastGen2 = [ v[len(v) - 1] for v in res2 ]
    return stats.kruskal(lastGen1, lastGen2)
# >>> hvalue("11.csv", "11-3.csv")
# KruskalResult(statistic=0.23874997725577796, pvalue=0.6251104082753336)    

def bor(exp):
    ''' best of run, exp is (f1, f2, ...)
    '''
    return tuple([run[-1] for run in r1] for r1 in exp)

import pandas as pd

def allStats(fs):
    fsa = np.array(fs)
    # print(fsa)
    # print(fsa.T)
    return { 'm_std': [ { "mean": np.mean(f), 'std1': np.std(f) } for f in fsa ], 
            'friedman': stats.friedmanchisquare(*fsa),
            'nemenyi': sp.posthoc_nemenyi_friedman(fsa.T) }
    
    stats.friedmanchisquare(*fs)

# def allStats(f1, f2, althyp='two-sided'): 
#     return { 'mean1': np.mean(f1), 'std1': np.std(f1), 'mean2': np.mean(f2), 'std2': np.std(f2), \
#                 'ttest': stats.ttest_ind(f1, f2, equal_var=False, alternative=althyp), 'utest': stats.mannwhitneyu(f1, f2,alternative=althyp)}

def diag(f1, f2):
    res1 = read(f1)
    lastGen1 = [ v[len(v) - 1] for v in res1 ]
    res2 = read(f2)
    lastGen2 = [ v[len(v) - 1] for v in res2 ]
    print(sorted(lastGen1))
    print(sorted(lastGen2))


import matplotlib.pyplot as plt
import matplotlib.lines as mlines
from matplotlib.axes import Axes

def charts(problems, file="test.png"): 
    ''' problems is [ [chart1Fs, chart2Fs, ...] ... ]
    chartFs is { [ f1, f2 ... ] + title info }
    f is all obtained fitnesses on all gens 
    so first we calc the size of chart 
    '''
    plt.clf()
    plt.subplots_adjust(hspace=1)
    fig, axs = plt.subplots(nrows=len(problems), ncols=len(problems[0]))

    # plts = []
    for (i, problemLine) in enumerate(problems): 
        for (j, problem) in enumerate(problemLine):
            ax = (axs[i,j] if type(axs) is np.ndarray else axs)
            for (k, f) in enumerate(problem['fs']): 
                fgens = np.transpose(f['data'])
                conf_int_gens = [ (mean, ) + stats.t.interval(0.95, len(gen)-1, loc=mean, scale=stats.sem(gen)) for gen in fgens for mean in [ np.mean(gen) ] ]
                gens = list(range(len(conf_int_gens)))
                
                ax.plot(gens, [m for (m, l, u) in conf_int_gens ], color=f['color'], label=f['title'], marker=f['marker'],ms=5, markevery=10, linewidth=1) #, linestyle="dotted")
                ax.fill_between(gens, [u for (m, l, u) in conf_int_gens ], [l for (m, l, u) in conf_int_gens ], color=[f['color'] + "20"])
            ax.legend(shadow=True, fancybox=True)
            if problem['title'] is not None:
                ax.set_title(problem['title'])  
# axs[1].set_xlabel('time (s)')
# axs[1].set_title('subplot 2')
# axs[1].set_ylabel('Undamped')
    fig.tight_layout()
    plt.savefig(file)


if __name__ == "__main__":
    if len(sys.argv) > 1:
        n = int(sys.argv[1]) 
        # rng = numpy.arange(5)
        for p in params:
            c = cmd(p)
            runs = run([], c, n)
            # print(runs)
            mean = np.mean(runs, axis=0)
            std = np.std(runs, axis=0)
            save(p + ".csv", runs, mean, std, np.mean(mean))
            # plt.clf()
            # # ticks = [i for i in range(1,51) if i % 5 == 0]
            # # plt.tick_params(axis='x', labelbottom=False)            
            # plt.boxplot(np.transpose(runs).tolist(), sym='')
            # plt.xticks(ticks=[1,11,21,31,41,51], labels=[0,10,20,30,40,50])
            # plt.savefig(p + '.png', bbox_inches='tight')


# plt.plot([1,2,3], [2,3,4])
# plt.show()


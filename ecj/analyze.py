import os
import re
import io
import csv 
import sys
import numpy

params = [ "11-3" ]
def cmd(p): 
    return f"cd target/classes && java ec.Evolve -file ec/app/multiplexerslow/{p}.params"

pattern = re.compile('(?<=Standardized=)(.*?)(?=\.)')
def run(acc, cmd, n, expectedLen = 52): 
    for i in range(n):
        print(f"Running test {i}")
        os.system(cmd);
        with open("target/classes/out.stat", "r") as statFile:
            txt = statFile.read()
        res = [ int(s) for s in pattern.findall(txt) ]
        if len(res) < expectedLen: 
            res = res + [0 for i in range(expectedLen - len(res))]
        acc.append(res)
    return acc

def save(name, runs, mean, std, meanofmean):
    with open(name, 'w', newline='') as csvfile:
        wr = csv.writer(csvfile)
        wr.writerows(runs)
        wr.writerow(['mean', 'std'])
        wr.writerow(mean)
        wr.writerow(std)
        wr.writerow(['meanofmean'])
        wr.writerow([meanofmean])

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

# //https://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U_test
# //https://projecteuclid.org/download/pdf_1/euclid.aoms/1177730491
# //https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.mannwhitneyu.html
def uvalue(f1, f2, alt='greater'):
    res1 = read(f1)
    lastGen1 = [ v[len(v) - 1] for v in res1 ]
    res2 = read(f2)
    lastGen2 = [ v[len(v) - 1] for v in res2 ]
    return stats.mannwhitneyu(lastGen1, lastGen2, alternative=alt)
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

def diag(f1, f2):
    res1 = read(f1)
    lastGen1 = [ v[len(v) - 1] for v in res1 ]
    res2 = read(f2)
    lastGen2 = [ v[len(v) - 1] for v in res2 ]
    print(sorted(lastGen1))
    print(sorted(lastGen2))


import matplotlib.pyplot as plt

if __name__ == "__main__":
    if len(sys.argv) > 1:
        n = int(sys.argv[1]) 
        rng = numpy.arange(5)
        for p in params:
            c = cmd(p)
            runs = run([], c, n)
            # print(runs)
            mean = numpy.mean(runs, axis=0)
            std = numpy.std(runs, axis=0)
            save(p + ".csv", runs, mean, std, numpy.mean(mean))
            plt.clf()
            # ticks = [i for i in range(1,51) if i % 5 == 0]
            plt.tick_params(axis='x', labelbottom=False)
            plt.xticks([])
            plt.boxplot(numpy.transpose(runs).tolist(), sym='')
            plt.savefig(p + '.png', bbox_inches='tight')


# plt.plot([1,2,3], [2,3,4])
# plt.show()


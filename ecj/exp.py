from analyze import * 

# results = \
#     [
#         [ { 'title': 'MUL-11',\
#             'files': ["data/bloat/11.csv", "data/bloat/11.neutral.csv"] },\
#             { 'title': 'PAR-12',\
#                 'files': ["data/bloat/parity.csv", "data/bloat/parity.neutral.csv"] } ],\
#         [{ 'title': 'MAJ-13',\
#             'files': [ "data/bloat/majority.csv", "data/bloat/majority.neutral.csv" ] },\
#             { 'title': 'CMP-5',\
#                 'files': [ "data/bloat/comparator.csv",  "data/bloat/comparator.neutral.csv"]\
#             } ]\
#     ]

results = \
    [
        [ { 'title': None, 'files': ["data/20/mul.csv", "data/20/mul.mut.csv", "data/20/mul.a.csv", "data/20/mul.n.2.csv"] } ]
        # [ { 'title': None, 'files': ["mul.csv", "mul.a.csv", "mul.n.csv"] } ]
        # [ { 'title': 'MUL-11', 'files': ["data/11/mul.csv", "mul.mut.csv", "data/11/mul.n.csv"] },
        #     { 'title': 'PAR-11', 'files': ["data/11/par.csv", "par.mut.csv", "data/11/par.n.csv"] } ],
        # [ { 'title': 'MAJ-11', 'files': ["data/11/maj.csv", "maj.mut.csv", "data/11/maj.n.csv"] },
        #     { 'title': 'CMP-11', 'files': ["data/11/cmp.csv", "cmp.mut.csv", "data/11/cmp.n.csv"] } ]
        # [ { 'title': None, 'files': ["data/11/par.csv", "data/11/par.a.csv", "data/11/par.n.csv"] } ]
        # [ { 'title': None, 'files': ["data/11/maj.csv", "data/11/maj.a.csv", "data/11/maj.n.csv"] } ]
        # [ { 'title': None, 'files': ["data/11/cmp.csv", "data/11/cmp.a.csv", "data/11/cmp.n.csv"] } ]
            # { 'title': 'PAR-11', 'files': ["par.csv", "par.a.csv", "par.n.csv"] } 
        # ],
        # [ { 'title': 'MAJ-11', 'files': ["maj.csv", "maj.a.csv", "maj.n.csv"] },
        #     { 'title': 'CMP-12', 'files': ["cmp.csv", "cmp.a.csv", "cmp.n.csv"] } 
        # ]
    ]

colors = [ {'color': '#cc4bc1', 'title':'RTsTx', 'marker':'x'}, { 'color': '#8a85f5', 'title': 'RTsTmx', 'marker':'o' }, { 'color': '#45c46f', 'title': 'RTsNaTx', 'marker':'v' }, { 'color': '#fa8b5f', 'title': 'RTsNTx', 'marker':'s' } ]

for resLine in results:
    for res in resLine:
        res['data'] = [ read(expFile) for expFile in res['files'] ]
        res['bors'] = bor(res['data'])
        res['stats'] = allStats(res['bors'])
        res['fs'] = [ {'data': r, 'color': colors[i]['color'], 'title':colors[i]['title'], 'marker': colors[i]['marker']} for (i, r) in enumerate(res['data']) ]
        print(f"Stats for {res['title']}:\n{res['stats']}\n ")

charts(results, "MUL-20.2.png") 


# # bors = bor(exps)
# # print(bors)
# #print(allStats(bor1, bor2, althyp="two-sided"))
# print(allStats(bors))

# bor1 = [run[-1] for run in r1]
# bor2 = [run[-1] for run in r2]

# print(bor1)
# print(bor2)

# #print(allStats(bor1, bor2, althyp="two-sided"))
# print(allStats(bor1, bor2, althyp="less"))


# charts([
#     [ 
#         {'title': 'MUX-11', 'fs': [{'data':r1, 'color': '#6060ee', 'title': 'RTsTx', 'marker':'o'}, {'data':r2, 'color': '#dd6060', 'title':'RTsNTx', 'marker':'s'}] },
#         {'title': 'MUX-11', 'fs': [{'data':r1, 'color': '#6060ee', 'title': 'RTsTx', 'marker':'o'}, {'data':r2, 'color': '#dd6060', 'title':'RTsNTx', 'marker':'s'}] }
#     ],
#     [ 
#         {'title': 'MUX-11', 'fs': [{'data':r1, 'color': '#6060ee', 'title': 'RTsTx', 'marker':'o'}, {'data':r2, 'color': '#dd6060', 'title':'RTsNTx', 'marker':'s'}] },
#         {'title': 'MUX-11', 'fs': [{'data':r1, 'color': '#6060ee', 'title': 'RTsTx', 'marker':'o'}, {'data':r2, 'color': '#dd6060', 'title':'RTsNTx', 'marker':'s'}] }
#     ]
# ])

bor1 = [run[-1] for run in r1]
bor2 = [run[-1] for run in r2]

print(bor1)
print(bor2)

#print(allStats(bor1, bor2, althyp="two-sided"))
print(allStats(bor1, bor2, althyp="greater"))


bor1 = [run[-1] for run in r1]
bor2 = [run[-1] for run in r2]

print(bor1)
print(bor2)

#print(allStats(bor1, bor2, althyp="two-sided"))
print(allStats(bor1, bor2, althyp="less"))
# print(allStats(bor1, bor2, althyp="two-sided"))

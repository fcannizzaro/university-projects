import csv
import sys
import time
import atexit

atexit.register(lambda: print(f'\n >> {time.time() - start}s\n\n'))

adj = {}

with open(sys.argv[1], 'r') as file:
		reader = csv.reader(file, delimiter=',')
		for a, b in list(reader):
			if a not in adj:
				adj[a] = []
			if b not in adj:
				adj[b] = []
			adj[a].append(b)	
			adj[b].append(a)

V = list(adj.keys())

T = 0

start = time.time()

local_coeff = {}

for v in V:
	local = 0
	k = len(adj[v])
	for u in adj[v]:
		for w in adj[v]:
			if u > w and w in adj[u]:
				local += 1
	local_coeff[v] = (2 * local) / (k * (k-1 or 1))
	
not_empty = len([ x for x in local_coeff.values() if x ])
cc_no_zeros = sum(local_coeff.values())/not_empty
cc_with_zeros = sum(local_coeff.values())/len(adj)
print(cc_no_zeros, cc_with_zeros)

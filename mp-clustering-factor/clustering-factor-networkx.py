import networkx as nx
import csv
import time
import atexit
import sys

atexit.register(lambda: print(f'\n >> {time.time() - start}s\n\n'))

G = nx.Graph()

with open(sys.argv[1], 'r') as file:
    reader = csv.reader(file, delimiter=',')
    for a, b in list(reader):
        G.add_edge(a, b)
        G.add_edge(b, a)

start = time.time()

degrees = nx.average_neighbor_degree(G).values()

print(nx.average_clustering(G, count_zeros=False))
print(nx.average_clustering(G, count_zeros=True))
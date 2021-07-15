import atexit
import itertools
import sys
import time

from pyspark import SparkConf, SparkContext

atexit.register(lambda: print(f'\n >> {time.time() - start}s\n\n'))

conf = SparkConf().setAppName('Clustering Coefficient')

conf = (conf.setMaster('local[8]')
        .set('spark.executor.memory', '14G')
        .set('spark.driver.memory', '14G')
        .set('spark.logConf', 'true')
        .set('spark.driver.maxResultSize', '14G'))

ctx = SparkContext(conf=conf)

graph = ctx \
    .textFile(sys.argv[1]) \
    .collect()

graph = ctx \
    .parallelize(graph) \
    .map(lambda line: tuple(line.split(','))) \
    .flatMap(lambda edge: [edge, edge[::-1]])


def expand_neighbours(group):
    node, neighbours = group
    cartesian = itertools.product(neighbours, neighbours)
    return [((u, v), node) for u, v in cartesian if u > v]


adjacency = graph.groupByKey()

start = time.time()

original = graph \
    .filter(lambda edge: edge[0] > edge[1]) \
    .map(lambda edge: (edge, '$'))

triplets = adjacency.flatMap(expand_neighbours)

all_triplets = triplets.count()

triplets = triplets \
    .union(original) \
    .groupByKey() \
    .map(lambda x: len(x[1]) - 1 if '$' in x[1] else 0) \
    .sum()

print(triplets / all_triplets)

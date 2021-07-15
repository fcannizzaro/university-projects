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

degrees = graph.countByKey()

def map_triplets(x):
    x = x[1]
    if '$' not in x:
        return []
    return [(y,) for y in x if y != '$']


def expand_neighbours(group):
    node, neighbours = group
    cartesian = itertools.product(neighbours, neighbours)
    return [((u, v), node) for u, v in cartesian if u > v]


adjacency = graph.groupByKey()

start = time.time()

original = graph \
    .filter(lambda edge: edge[0] > edge[1]) \
    .map(lambda edge: (edge, '$'))

triplets = adjacency \
    .flatMap(expand_neighbours) \
    .union(original) \
    .groupByKey() \
    .flatMap(map_triplets) \
    .countByKey()


def local_coefficient(pair):
    node, edges = pair
    degree = degrees[node]
    return 2 * edges / (degree * (degree - 1))


cc = ctx \
    .parallelize(triplets.items()) \
    .map(local_coefficient) \
    .reduce(lambda a, b: a + b)

cc_no_zeros = cc / len(triplets)
cc_with_zeros = cc / len(degrees)
print(cc_no_zeros, cc_with_zeros)

from network import k_fold
import numpy as np

shape = (150, 230, 1)

metrics = k_fold('train', 'genuine', shape, 20)

avg = np.mean(metrics, axis=0)

metrics = k_fold('train', 'forgeries', shape, 30)

avg = np.mean(metrics, axis=0)

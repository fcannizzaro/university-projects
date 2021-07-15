import matplotlib.pyplot as plt
import numpy as np


def save_figure(metrics, dataset, epochs, lrs, field='accuracy', fold=0):
    plt.clf()
    y1 = [metric[field] for metric in metrics]
    y2 = [metric['val_' + field] for metric in metrics]
    is_accuracy = field == 'accuracy'
    x = np.arange(1, len(metrics) + 1)
    plt.plot(x, y1, label='train', color='green' if is_accuracy else 'red')

    if not dataset.startswith('reference'):
        plt.plot(x, y2, label='validation', color='blue' if is_accuracy else 'orange')

    y_max = max(y1 + y2) + 0.1
    epc = np.cumsum(epochs)
    for i in range(len(epc)):
        plt.axvline(x=epc[i], color=(0, 0, 0, 0.2))
        plt.text(epc[i] - 5 - len(f'ε={lrs[i]}'), y_max / 2, f'ε={lrs[i]}', color=(0, 0, 0, 0.4), fontsize=9)

    plt.yticks(np.arange(0, y_max, y_max/10))
    plt.xticks(np.arange(1, len(metrics), 10))
    plt.xlabel('epochs')
    plt.ylabel(field)

    plt.title(f"{dataset} {field}")
    plt.legend()
    # plt.show()
    plt.savefig(f'{dataset}-{field}-{fold}.png')

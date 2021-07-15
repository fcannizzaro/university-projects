import os
import imageio
import numpy as np
import tensorflow as tf
from sklearn.model_selection import train_test_split, KFold
from tensorflow.keras.layers import Dense, Conv2D, Flatten, Dropout, MaxPool2D
from tensorflow.keras.models import Sequential
from tensorflow.keras.optimizers import SGD
from tensorflow.keras.callbacks import TensorBoard, LambdaCallback
from tensorflow.keras.utils import to_categorical
import datetime
import util


def make_model(shape: tuple, n_classes: int, weights: str = None) -> Sequential:
    mod = Sequential([
        Conv2D(input_shape=shape, kernel_size=(5, 5), strides=1, filters=128, activation='relu', padding='same'),
        MaxPool2D(pool_size=(2, 2), strides=2, padding='same'),
        Dropout(0.5),
        Flatten(),
        Dense(96, activation='relu'),
        Dropout(0.25),
        Dense(54, activation='relu'),
        Dropout(0.25),
        Dense(n_classes, activation='softmax')
    ])
    return mod


def train_model(data, model, epochs: int = 50, dataset: str = None, fold=0):
    model.summary()

    x_train, y_train, x_test, y_test = data
    epcs = [epochs, epochs // 2, epochs // 2]
    lrs = [0.01, 0.001, 0.0001]

    epochs_metrics = []

    for i in range(len(lrs)):
        # log_dir="logs\\fit\\" + datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
        # tensorboard_callback = TensorBoard(log_dir=log_dir, histogram_freq=1)
        log_cb = LambdaCallback(on_epoch_end=lambda epoch, logs: epochs_metrics.append(logs))
        model.compile(loss="categorical_crossentropy", optimizer=SGD(lr=lrs[i]), metrics=["accuracy"])
        history = model.fit(x_train, y_train, batch_size=64, epochs=epcs[i], shuffle=True, verbose=1,
                            callbacks=[log_cb], validation_data=(x_test, y_test), use_multiprocessing=True)

    loss, accuracy = model.evaluate(x_test, y_test, batch_size=64, verbose=1)
    util.save_figure(epochs_metrics, dataset, epcs, lrs, fold=fold)
    util.save_figure(epochs_metrics, dataset, epcs, lrs, 'loss', fold=fold)

    return loss, history.history['accuracy'][-1], accuracy, epochs_metrics


def final_model(model, final_n_classes: int = 0, freeze=False):
    model.pop()
    model.trainable = not freeze
    new_mod = Sequential([model, Dense(final_n_classes, activation='softmax')])
    return new_mod


def k_fold(dataset, category, shape, epochs):
    all_metrics = []
    data, labels, n_classes, _ = load_dataset(dataset, category, shape, all_data=True)
    kfold = KFold(n_splits=6, random_state=None, shuffle=True)
    fold = 1

    for train_index, test_index in kfold.split(data):
        x_train, x_test = data[train_index], data[test_index]
        y_train, y_test = to_categorical(labels[train_index], n_classes), to_categorical(labels[test_index], n_classes)
        model = make_model(shape, n_classes)
        metrics = train_model((x_train, y_train, x_test, y_test), model, epochs=epochs, dataset=category, fold=fold)
        loss, accuracy, val_accuracy, epochs_metrics = metrics
        all_metrics.append([loss, accuracy, val_accuracy])
        print(f'FOLD {fold}: loss=[{loss}], accuracy=[{accuracy}], validation_accuracy=[{val_accuracy}]')
        fold += 1

    return all_metrics


def load_dataset(dataset, category, shape, all_data: bool = False, only: str = None):
    directory = f'{dataset}/dutch/{category}-gray/'
    data, labels, validity = [], [], []

    for file in os.listdir(directory):
        img = imageio.imread(directory + file) / 255.0
        is_genuine = file[-5:-4] == 'g'
        if not only or (only == 'genuine' and is_genuine) or (only == 'forged' and not is_genuine):
            data.append(img)
            labels.append(file[:3])
            validity.append(is_genuine)

    labels = np.array(list(map(int, labels)))
    data = np.array(data)
    n_classes = max(labels) + 1

    if all_data:
        return data.reshape((data.shape[0], *shape)), labels, n_classes, validity

    x_train, x_test, y_train, y_test = train_test_split(data, labels, test_size=0.205)
    x_train = x_train.reshape((x_train.shape[0], *shape))
    x_test = x_test.reshape((x_test.shape[0], *shape))

    train_labels = to_categorical(y_train, n_classes)
    test_labels = to_categorical(y_test, n_classes)

    return (x_train, train_labels, x_test, test_labels), n_classes

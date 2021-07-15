## Signature Verification Using a Convolutional Neural Network
[Cozzens, B. et al. “Signature Verification Using a Convolutional Neural Network.” (2017).](https://www.semanticscholar.org/paper/Signature-Verification-Using-a-Convolutional-Neural-Cozzens-Huang/ea1fcaeee53487e7995e0657bd93ccb887ac2d23#paper-header)

### Rete

```python
Sequential([
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
```

### Dataset

- Train
	- 230 Firme Vere  (genuine - 10 firmatori)
	- 123 Firme False (forged - 10 firmatori)
- Test
	- 646 Firme Vere  (reference -  54 firmatori)
	- 1287 Firme (648 firme vere + 641 firme false) 

Per ogni allenamento è stato considerato il **20%** come **validation set** e la parte restante come **training set**.

L'obiettivo della rete consiste nell'allenare 2 classificatori, uno per le firme vere e uno per le firme false. Successivamente, per generalizzare, il classificatore delle firme vere e false viene allenato anche sulle firme di reference.

# Genuine Classifier

Non essendo le classi (i firmatori) identificati con un id continuo nel train set (es.  è presente il firmatore 14 ma non il 13, che è possibile invece trovarlo nel reference set) è stato considerato come numero di classi totali il max fra tutti gli id + 1 (17). Effettivamente solamente 10 classi sono state allenate nel classificatore. *La stessa situazione si presenta nel forged classifier.*

### Metriche
60 epoche divise in differenti *learning rate*.

| Accuracy | Loss |
|--- | --- |
|   ![genuine-accuracy](https://raw.githubusercontent.com/fcannizzaro/university-projects/master/signature-verification-using-a-convolutional-neural-network/report/genuine-accuracy.png)   |   ![genuine-loss](https://raw.githubusercontent.com/fcannizzaro/university-projects/master/signature-verification-using-a-convolutional-neural-network/report/genuine-loss.png)   |


Effettuando un **6-fold** (oltre non è possibile per motivi di memoria) si ottengono le seguenti metriche medie:

| Loss       | Accuracy   | Validation Accuracy |
| ---------- | ---------- | ------------------- |
| 0.27510549 | 0.97559044 | 0.95405983          |

# Forged Classifier

Lo stesso procedimento è stato ripetuto con le firme false. In questo caso l'accuratezza risulta inferiore anche a causa del numero di differenti firmatori per ogni firma.

### Metriche
100 epoche divise in differenti *learning rate*.

| Accuracy | Loss |
|--- | --- |
|   ![forged-accuracy](https://raw.githubusercontent.com/fcannizzaro/university-projects/master/signature-verification-using-a-convolutional-neural-network/report/forged-accuracy.png)   |   ![forged-loss](https://raw.githubusercontent.com/fcannizzaro/university-projects/master/signature-verification-using-a-convolutional-neural-network/report/forged-loss.png)   |


Effettuando un **6-fold** (oltre non è possibile per motivi di memoria) si ottengono le seguenti metriche medie:

| Loss  | Accuracy | Validation Accuracy |
| ----- | -------- | ------------------- |
| 0.863 | 0.98     | 0.80                |

# Reference Classifier (Genuine+)

Questo classificatore, ha come base il **Genuine Classifier**, con i pesi già calcolati, a cui è stato sostituito l'ultimo livello (Dense) rispetto al nuovo numero di classi da classificare e d è stato ri-allenato con i dati del test set. Lo stesso tipo di allenamento è stato effettuato sul **Forged Classifier** ma ovviamente presenta una accuratezza molto inferiore.

### Metriche
100 epoche divise in differenti *learning rate*.

| Accuracy | Loss |
|--- | --- |
|   ![reference-accuracy](https://raw.githubusercontent.com/fcannizzaro/university-projects/master/signature-verification-using-a-convolutional-neural-network/report/reference-genuine-accuracy.png)   |   ![reference-loss](https://raw.githubusercontent.com/fcannizzaro/university-projects/master/signature-verification-using-a-convolutional-neural-network/report/reference-genuine-loss.png)   |


Effettuando un **6-fold** (oltre non è possibile per motivi di memoria) si ottengono le seguenti metriche medie:

| Loss  | Accuracy | Validation Accuracy |
| ----- | -------- | ------------------- |
|  1.0  | 0.851    | 0.797               |

```python
model.pop()
model.trainable = False
new_model = Sequential([model, Dense(final_n_classes, activation='softmax')])
```
Ma allenando questa nuova rete, anche con molte epoche, l'accuracy rimane vicina a 0. Inoltre nel codice citato dall'articolo non viene mostrato l'uso di alcuna forma di freezing dei modelli.

## Obbiettivo Finale

L'obbiettivo dell'articolo è quello di riconoscere se una firma sia vera/falsa.

I risultati degli autori vengono mostrati solamente sulla parte "genuine" del questioned set:

```python
# genuine classifier predictions
ref_predict = genuine_model.predict(x_test)
ref_classes = tf.argmax(ref_predict, axis=1).numpy()

# forged classifier predictions
forged_predict = forged_model.predict(x_test)
forged_classes = tf.argmax(forged_predict, axis=1).numpy()

same_signature = forged_classes == ref_classes
acc_genuine = np.sum(ref_classes == y_test)
acc_forged = np.sum(forged_classes == y_test)
```

Vengono calcolate le predizioni per entrambi i classificatori con i pesi finali e viene tracciato il numero di firme correttamente individuate dai due classificatori e in quali casi entrambi hanno individuato lo stesso autore.

Per questo ultimo caso viene eseguita un rapporto sulle probabilità per singola firma individuata da entrambi i classificatori:

$$
validity_i = \frac{p\_forged_i}{p\_genuine_i}
$$

Se questo rapporto:

$$
validity_i \ge 0.4
$$

allora la firma *i* può considerarsi falsa.

Dall'articolo:

> After modifying the weights for the reference set, we put
> each signature through the network, first using the genuine
> signature weights, then the forged signature weights. If the
> maximum probabilities of the output vectors from each output
> don’t indicate the same label, we compare the probability label
> to the real label, and track the accuracy of each classifier. If
> the classifiers match, we put the maximum probabilities in a
> ratio that gives the percentage that a signature is forged. If the
> match for a forgery in this ratio is greater than forty percent,
> the signature is classified as forged, favoring classifying
> signatures as genuine.

### Risultati

Il dataset su cui è stato effettuato il test è stato estratto dal test/questioned e si compone solamente di **648** firme vere.

Mediati da più esecuzioni:

| Classificatore                  | Accuracy |
| ------------------------------- | -------- |
| Final Genuine Classifier        | 75%      |
| Final Forged Classifier         | 73%      |
| Combinazione dei classificatori | //       |

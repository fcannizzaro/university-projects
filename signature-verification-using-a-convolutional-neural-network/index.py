import numpy as np
import tensorflow as tf

from network import make_model, train_model, load_dataset, final_model

shape = (150, 230, 1)

# Datasets
x_test, y_test, test_n_classes, validity = load_dataset('test', 'questioned', shape, all_data=True, only='genuine')
reference_dataset, ref_n_classes = load_dataset('test', 'reference', shape)
genuine_dataset, genuine_n_classes = load_dataset('train', 'genuine', shape)
forged_dataset, forged_n_classes = load_dataset('train', 'forgeries', shape)

# genuine
genuine_model = make_model(shape, genuine_n_classes)
loss, accuracy, val_accuracy, epochs_metrics = train_model(genuine_dataset, genuine_model, epochs=30, dataset='genuine')
print(f'Genuine Metrics [loss] {loss} [train accuracy] {accuracy} [validation accuracy] {val_accuracy}')

# forgeries
forged_model = make_model(shape, forged_n_classes)
loss, accuracy, val_accuracy, _ = train_model(forged_dataset, forged_model, epochs=50, dataset='forged')
print(f'Forged Metrics [loss] {loss} [train accuracy] {accuracy} [validation accuracy] {val_accuracy}')

# reference forgeries
genuine_model = final_model(genuine_model, ref_n_classes, freeze=False)
loss, accuracy, val_accuracy, _ = train_model(reference_dataset, genuine_model, epochs=50,
                                              dataset='reference-genuine')
print(f'Reference Metrics [loss] {loss} [train accuracy] {accuracy} [validation accuracy] {val_accuracy}')

# reference forgeries
forged_model = final_model(forged_model, ref_n_classes, freeze=False)
loss, accuracy, val_accuracy, _ = train_model(reference_dataset, forged_model, epochs=50,
                                              dataset='reference-forged')
print(f'Reference Forged Metrics [loss] {loss} [train accuracy] {accuracy} [validation accuracy] {val_accuracy}')

# genuine classifier predictions
ref_predict = genuine_model.predict(x_test)
ref_classes = tf.argmax(ref_predict, axis=1).numpy()

# forged classifier predictions
forged_predict = forged_model.predict(x_test)
forged_classes = tf.argmax(forged_predict, axis=1).numpy()

same_signature = forged_classes == ref_classes
acc_genuine = np.sum(ref_classes == y_test)
acc_forged = np.sum(forged_classes == y_test)

print('Total Signatures =', len(ref_classes))
print('[Genuine Classifier] Reference Signatures', acc_genuine)
print('[Forged Classifier]  Reference Signatures', acc_forged)
print('[Classifiers]        Same Signature', np.sum(same_signature))

accuracy = []
classifications = []

for i in range(len(same_signature)):
    if same_signature[i]:
        forged_prob, genuine_prob = np.max(forged_predict[i]), np.max(ref_predict[i])
        is_genuine = forged_prob / genuine_prob <= .4
        accuracy.append(is_genuine is True)
        classifications.append((y_test[i], forged_classes[i], forged_prob, ref_classes[i], genuine_prob, is_genuine))

accuracy_genuine = np.sum(acc_genuine) / len(ref_classes)
accuracy_forged = np.sum(acc_forged) / len(ref_classes)
final_accuracy = np.sum(accuracy) / len(accuracy)

print('[Genuine Classifier] Accuracy', accuracy_genuine)
print('[Forged Classifier] Accuracy', accuracy_forged)
print(final_accuracy)

for signature, forged_signature, forged_prob, genuine_signature, genuine_prob, is_genuine in classifications:
    print(
        f'[is-genuine={is_genuine}] [Signature={signature}], [Forged={forged_signature}, {forged_prob}], [Genuine={genuine_signature}, {genuine_prob}]')

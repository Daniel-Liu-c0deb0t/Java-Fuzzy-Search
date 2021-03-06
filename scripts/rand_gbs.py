import os
import sys
import random
from dna_random_gen import rand_edits, rand_str

random.seed(0)

hamming = False
num_barcodes = 48
barcode_lengths = list(range(8, 15 + 1))
edits = 1
length = 100
num_iter = 10000000

barcodes = []

with open("barcodes.txt", "w") as f:
    for i in range(num_barcodes):
        barcode_length = random.choice(barcode_lengths)
        barcode = rand_str(barcode_length)
        barcodes.append(barcode)
        f.write("\"barcode_%d\": \"%s\"\n" % (i, barcode))

with_gbs = 0

os.makedirs("gbs_result", exist_ok = True)
files = [open("gbs_result/has_gbs_barcode_%d.txt" % i, "w") for i in range(len(barcodes))]

for i in range(num_iter):
    print("@" + str(i))

    dna_barcode = ""
    barcode_idx = None

    if random.randint(0, 1) == 0:
        barcode_idx = random.randint(0, len(barcodes) - 1)
        dna_barcode = barcodes[barcode_idx]
        dna_barcode = rand_edits(dna_barcode, edits, hamming)

    dna = dna_barcode + rand_str(length)

    if dna_barcode:
        files[barcode_idx].write(str(i) + "\n")
        with_gbs += 1

    print(dna)
    print("+")
    print("A" * len(dna))

print("Reads with barcode: " + str(with_gbs), file = sys.stderr)

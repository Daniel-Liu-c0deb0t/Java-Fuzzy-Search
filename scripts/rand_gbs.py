import sys
import random
from dna_random_gen import rand_edits, rand_str

random.seed(0)

adapter_length = 30
enzyme_length = 5
num_barcodes = 48
barcode_lengths = list(range(4, 8 + 1))
edits = 1
length = 100
num_iter = 1000000

adapter = rand_str(adapter_length)
print("Adapter: " + adapter, file = sys.stderr)

enzyme = rand_str(enzyme_length)
print("Enzyme: " + enzyme, file = sys.stderr)

barcodes = []

with open("barcodes.txt", "w") as f:
    for i in range(num_barcodes):
        barcode_length = random.choice(barcode_lengths)
        barcode = rand_str(barcode_length)
        barcodes.add(barcode)
        f.write("\"barcode_%d\": \"%s\"\n" % (i, barcode))

with_gbs = 0

with open("has_gbs.txt", "w") as f:
    for i in range(num_iter):
        print("@" + str(i))

        dna_barcode = ""
        dna_enzyme = ""
        dna_adapter = ""

        if random.randint(0, 1) == 0:
            dna_barcode = rand_edits(random.choice(barcodes), edits)

        if random.randint(0, 1) == 0:
            dna_enzyme = rand_edits(enzyme, edits)

        if random.randint(0, 1) == 0:
            dna_adapter = rand_edits(adapter, edits)

        dna = dna_barcode + dna_enzyme + rand_str(length) + dna_adapter

        if dna_barcode and dna_enzyme and dna_adapter:
            f.write(str(i) + "\n")
            with_gbs += 1

        print(dna)
        print("+")
        print("A" * len(dna))

print("Reads with GBS: " + str(with_gbs), file = sys.stderr)

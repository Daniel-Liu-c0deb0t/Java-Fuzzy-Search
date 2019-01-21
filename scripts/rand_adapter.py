import sys
import random
from dna_random_gen import rand_edits, rand_str

random.seed(0)

adapter_length = 30
edits = 1
length = 100
num_iter = 10000000

adapter = rand_str(adapter_length)
print("Adapter: " + adapter, file = sys.stderr)

with_adapter = 0

with open("has_adapter.txt", "w") as f:
    for i in range(num_iter):
        print("@" + str(i))

        if random.randint(0, 3) < 3:
            dna = rand_str(length) + rand_edits(adapter, edits)
            f.write(str(i) + "\n")
            with_adapter += 1
        else:
            dna = rand_str(length + adapter_length)

        print(dna)
        print("+")
        print("A" * len(dna))

print("Reads with adapter: " + str(with_adapter), file = sys.stderr)

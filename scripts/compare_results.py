import sys

correct_file = sys.argv[1]
fastq_file = sys.argv[2]

correct_idx = set()

with open(correct_file, "r") as f:
    l = f.readline().strip()

    if l:
        correct_idx.add(int(l))

with open(fastq_file, "r") as f:
    l = f.readline().strip()
    f.readline()
    f.readline()
    f.readline()

    if l:
        idx = int(l[1:])

        if idx in correct_idx:
            correct += 1
        else:
            wrong += 1

print("Correct reads: " + str(correct))
print("Wrong reads: " + str(wrong))

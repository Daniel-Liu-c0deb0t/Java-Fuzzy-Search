def compare(correct_file, fastq_file):
    correct_idx = set()

    with open(correct_file, "r") as f:
        while True:
            l = f.readline().strip()

            if l:
                correct_idx.add(int(l))
            else:
                break

    correct = 0
    wrong = 0

    with open(fastq_file, "r") as f:
        while True:
            l = f.readline().strip()
            f.readline()
            f.readline()
            f.readline()

            if l:
                idx = int(l[1:])

                if idx in correct_idx:
                    correct_idx.remove(idx)
                    correct += 1
                else:
                    wrong += 1
            else:
                break

    return correct, wrong, len(correct_idx)

if __name__ == "__main__":
    import sys
    correct_file = sys.argv[1]
    fastq_file = sys.argv[2]
    correct, wrong, missing = compare(correct_file, fastq_file)
    print("Correct reads: " + str(correct))
    print("Wrong reads: " + str(wrong))
    print("Missing reads: " + str(missing))

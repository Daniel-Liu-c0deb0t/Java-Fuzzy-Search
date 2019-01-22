import sys
from compare_results import compare

n = int(sys.argv[1])

total = 0
total_missing = 0
total_wrong = 0

for i in range(n):
    correct, wrong, missing = compare(sys.argv[2] % i, sys.argv[3] % i)
    total += correct + missing
    total_missing += missing
    total_wrong += wrong

print("Total: " + str(total))
print("Total missing: " + str(total_missing))
print("Total wrong: " + str(total_wrong))

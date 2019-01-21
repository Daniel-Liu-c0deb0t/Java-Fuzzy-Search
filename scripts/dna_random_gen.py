import random

a = "ATCG"

def rand_edits(s, k):
    edits = random.randint(0, k)
    l = list(s)

    for _ in range(edits):
        edit_type = random.randint(0, 2)

        if edit_type == 0:
            idx = random.randint(0, len(s) - 1)
            l[idx] = random.choice(list(set(a) - set(s[idx])))
        elif edit_type == 1:
            idx = random.randint(0, len(s) - 1)
            l.pop(idx)
        else:
            idx = random.randint(0, len(s))
            l.insert(idx, random.choice(a))

    return "".join(l)

def rand_str(n):
    l = []

    for _ in range(n):
        l.append(random.choice(a))

    return "".join(l)

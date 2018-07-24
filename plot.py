import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import sys

df = pd.read_csv(sys.argv[1])

rows = int(sys.argv[2])

for column in df:
    if column not in ["area", "distance"]:
        x = df["distance"][:rows]
        y = df[column][:rows]

        plt.plot(x, y, label=column)

plt.legend()
plt.show()

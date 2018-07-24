import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import sys

rows = int(sys.argv[2])

csv = np.genfromtxt(sys.argv[1], delimiter=',')
x = csv[:,1][:rows]
y1 = csv[:,2][:rows]
y2 = csv[:,3][:rows]

plt.plot(x, y1)
plt.plot(x, y2)

plt.show()

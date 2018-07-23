import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

rows = 28882

csv = np.genfromtxt('out.csv', delimiter=',')
x = csv[:,1][:rows]
y1 = csv[:,2][:rows]
y2 = csv[:,3][:rows]

plt.plot(x, y1)
plt.plot(x, y2)

plt.show()

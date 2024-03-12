#!/usr/bin/env python
# coding: utf-8

# In[1]:
import os
import sys

import matplotlib.pyplot as plt
import numpy as np

from importlib import reload

import DataReader as DataReader
import Plotter as Plotter


# In[2]:


reload(DataReader)
reload(Plotter)


# In[3]:

projects = [ "semiDisplayOpticNet"]
switch_sizes = [ 256 ]
num_simulations = 30
datasets = [ "tor" ]
num_nodes = [ 128 ]
mus = [ 4 ]

if not os.path.exists(f"output/{sys.argv[1]}"):
    os.makedirs(f"output/{sys.argv[1]}")


# In[4]:


tor_data = []


# In[5]:

for project in projects:
    for dataset in datasets:
        for num_node in num_nodes:
            for switch_size in switch_sizes:
                for mu in mus:
                    tor_data.append(
                        DataReader.DataReader(
                            dataset, project, num_node, switch_size, num_simulations, mu
                        )
                    )

tor_data = np.array(tor_data)

# In[7]:


slc = [ i for i in range(len(tor_data)) ]


# In[8]:


print(tor_data[slc])


# In[9]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Active switches per round")
ax.set_xlabel("Rounds")
ax.set_ylabel("Switches Percentage")

for data in tor_data[slc]:
    Plotter.Plotter.cdf_active_switches(data.cdf_active_switches(), ax)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/active_switches.png", dpi=300, transparent=False)
plt.close(fig)

print("finish")

# In[10]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF \% of active ports for switches")
ax.set_xlabel("Ports Percentage")
ax.set_ylabel("Switch Percentage")

for data in tor_data[slc]:
    Plotter.Plotter.cdf_switches_active_ports(data.cdf_switch_active_ports(), ax)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/switches_active_ports.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")

# In[11]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Active Ports per Round")
ax.set_xlabel("Rounds")
ax.set_ylabel("Ports Percentage")

for data in tor_data[slc]:
    Plotter.Plotter.cdf_active_ports(data.cdf_active_ports(), ax)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/active_ports.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")

# In[ ]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Routings per Node")
ax.set_xlabel("Routing")
ax.set_ylabel("Node Percentage")

for data in tor_data[slc]:
    Plotter.Plotter.cdf_routings(data.cdf_node_routings(), ax)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/node_routings.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")

# In[ ]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Alterations per Node")
ax.set_xlabel("Alterations")
ax.set_ylabel("Node Percentage")

for data in tor_data[slc]:
    Plotter.Plotter.cdf_alterations(data.cdf_node_alterations(), ax)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/node_alterations.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")

# In[ ]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Routings per Switch")
ax.set_xlabel("Routing")
ax.set_ylabel("Switches Percentage")

for data in tor_data[slc]:
    Plotter.Plotter.cdf_routings(data.cdf_switch_routings(), ax)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/switch_routings.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")

# In[ ]:


fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title("CDF Alterations per Switch")
ax.set_xlabel("Alterations")
ax.set_ylabel("Switches Percentage")

for data in tor_data[slc]:
    Plotter.Plotter.cdf_alterations(data.cdf_switch_alterations(), ax)

ax.legend([
    f"{data.project}--{data.dataset}--{data.num_nodes}--{data.num_switches}"
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/switch_alterations.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")
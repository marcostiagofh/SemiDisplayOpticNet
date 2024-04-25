#!/usr/bin/env python
# coding: utf-8

# In[1]:
import os
import sys
import scienceplots
import matplotlib.pyplot as plt
import numpy as np

from importlib import reload

import DataReader as DataReader
import Plotter as Plotter


# In[2]:


reload(DataReader)
reload(Plotter)


# In[3]:

projects = [ #"semiDisplayOpticNet",
"semiDisplayOpticNetHL",
#"semiDisplayOpticNetHLAP",
#"cbOptNet",
#"cbOptNetHL"
#,"SplayOpticNet"
]

num_datasets = 2
switch_sizes = [ [16,32,64,128,256],[16,32,92,162,734],[114,128,256,456,2048],[128,256,2048],[16,32,64,288],[16,32,256],[16,32,256] ]
num_simulations = [ 30,1,1,1,1,1,30 ]
datasets = [ 
["bursty-0.4-1"], 
["facebookDS"], 
["hpcDS-exact_boxlib_cns_nospec_large"], 
["normalDS-0.2"],
["pfabDS-trace_0_5"],
["skewed-1-0.4"],
["tor"] 
]
num_nodes = [ [128],[367],[1024],[1024],[144],[128],[128]]
mus = [ [4],[4],[4],[4],[4],[4],[4] ]

output_folder = [ 
"bursty-0.4-1", 
"facebookDS", 
"hpcDS-exact_boxlib_cns_nospec_large", 
"normalDS",
"pfabDS-trace",
"skewed-1-0.4",
"tor"
]

# In[5]:
for i in range(num_datasets):
    if not os.path.exists(f"output/{output_folder[i]}"):
        os.makedirs(f"output/{output_folder[i]}")

    tor_data = []

    for project in projects:
        for dataset in datasets[i]:
            for num_node in num_nodes[i]:
                for switch_size in switch_sizes[i]:
                    for mu in mus[i]:
                        tor_data.append(
                            DataReader.DataReader(
                                dataset, project, num_node, switch_size, num_simulations[i], mu
                            )
                        )

    tor_data = np.array(tor_data)

    # In[7]:

    slc = [ i for i in range(len(tor_data)) ]

    # In[8]:

    print(tor_data[slc])

    # Collect unique values of data.switch_size
    unique_switch_sizes = set(data.switch_size for data in tor_data[slc])

    # Define a list of unique colors
    colors = plt.cm.tab10.colors[:len(unique_switch_sizes)]  # Using a predefined colormap for colors

    # Create a dictionary mapping each unique switch_size to a unique color
    color_dict = dict(zip(unique_switch_sizes, colors))

    # In[9]:
    
    '''
    fig, ax = plt.subplots(figsize=(7, 4))
    ax.set_ylabel("Link Updates 10**3")
    ax.set_xlabel(datasets[i][0]) #"Project")
    ax.set_ylabel("Work 10**3")    
    
    if "facebook" in datasets[i][0] or "hpcDS" in datasets[i][0] or "pfabDS" in datasets[i][0]:
        ax.set_ylim(0, 35)  # Setting y-axis limit
        Plotter.Plotter.cbnet_link_updates(tor_data[slc], normalize=1e3, ax=ax)       
    else:    
        ax.set_ylabel("Work 10**3")
        ax.set_ylim(0, 2.5)  # Setting y-axis limit
        Plotter.Plotter.cbnet_link_updates(tor_data[slc], normalize=1e3, ax=ax)

    ax.plot()
    fig.savefig(f"output/{output_folder[i]}/cbnet_link_updates.png", dpi=300, transparent=False)
    plt.close(fig)

    print("finish")
    '''
    
    fig, ax = plt.subplots(figsize=(7, 4))
    ax.set_xlabel(datasets[i][0])
    if "facebook" in datasets[i][0] or "hpcDS" in datasets[i][0] or "pfabDS" in datasets[i][0]:
        ax.set_ylabel("Work 10**6")
        ax.set_ylim(0, 60)  # Setting y-axis limit
        Plotter.Plotter.total_work_link_updates(tor_data[slc], normalize=1e6, ax=ax)        
    else:    
        ax.set_ylabel("Work 10**4")
        ax.set_ylim(0, 40)  # Setting y-axis limit
        Plotter.Plotter.total_work_link_updates(tor_data[slc], normalize=1e4, ax=ax)

    ax.plot()
    fig.savefig(f"output/{output_folder[i]}/total_work.png", dpi=300, transparent=False)
    plt.close(fig)

    print("finish")

    fig, ax = plt.subplots(figsize=(7, 4))
    ax.set_xlabel(datasets[i][0])
    
    if "facebook" in datasets[i][0] or "hpcDS" in datasets[i][0] or "pfabDS" in datasets[i][0]:
        ax.set_ylabel("Work 10**5")
        ax.set_ylim(0, 52)  # Setting y-axis limit
        Plotter.Plotter.total_rounds( tor_data[slc], normalize=1e5, ax=ax)      
    else:
        ax.set_ylabel("Rounds 10**3")
        ax.set_ylim(0, 54)  # Setting y-axis limit
        Plotter.Plotter.total_rounds( tor_data[slc], normalize=1e3, ax=ax)

    ax.plot()
    fig.savefig(f"output/{output_folder[i]}/rounds.png", dpi=300, transparent=False)
    plt.close(fig)

    print("finish")
    '''
    # In[10]:
    
    fig, ax = plt.subplots(figsize=(8, 4))
    ax.set_title("CDF Active switches per round")
    ax.set_xlabel("Rounds")
    ax.set_ylabel("Switches Percentage")

    for data in tor_data[slc]:
        Plotter.Plotter.cdf_active_switches(data.cdf_active_switches(), ax)

    ax.legend([
        f"{Plotter.Plotter.get_project_name(data)}--{data.dataset}--{data.num_nodes}--{data.num_switches}"
        for data in tor_data[slc]
    ], loc="best", frameon=False)

    ax.plot()
    fig.savefig(f"output/{output_folder[i]}/active_switches.png", dpi=300, transparent=False)
    plt.close(fig)

    print("finish")

    fig, ax = plt.subplots(figsize=(8, 4))
    ax.set_title("CDF \% of active ports for switches")
    ax.set_xlabel("Ports Percentage")
    ax.set_ylabel("Switch Percentage")

    for data in tor_data[slc]:
        color = color_dict[data.switch_size]  # Get color based on switch_size
        Plotter.Plotter.cdf_switches_active_ports(data.cdf_switch_active_ports(), ax, color=color)

    ax.legend([
        f"{Plotter.Plotter.get_project_name(data)}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
        for data in tor_data[slc]
    ], loc="best", frameon=False)

    ax.plot()
    fig.savefig(f"output/{output_folder[i]}/switches_active_ports.png", dpi=300, transparent=False)
    plt.close(fig)
    print("finish")

    # In[11]:


    fig, ax = plt.subplots(figsize=(8, 4))
    ax.set_title("CDF Active Ports per Round")
    ax.set_xlabel("Rounds")
    ax.set_ylabel("Ports Percentage")

    for data in tor_data[slc]:
        color = color_dict[data.switch_size]  # Get color based on switch_size
        Plotter.Plotter.cdf_active_ports(data.cdf_active_ports(), ax, color=color)

    ax.legend([
        f"{Plotter.Plotter.get_project_name(data)}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
        for data in tor_data[slc]
    ], loc="best", frameon=False)

    ax.plot()
    fig.savefig(f"output/{output_folder[i]}/active_ports.png", dpi=300, transparent=False)
    plt.close(fig)
    print("finish")

    # In[ ]:


    fig, ax = plt.subplots(figsize=(8, 4))
    ax.set_title("CDF Routings per Node")
    ax.set_xlabel("Routing")
    ax.set_ylabel("Node Percentage")

    for data in tor_data[slc]:
        color = color_dict[data.switch_size]  # Get color based on switch_size
        Plotter.Plotter.cdf_routings(data.cdf_node_routings(), ax, color=color)

    ax.legend([
        f"{Plotter.Plotter.get_project_name(data)}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
        for data in tor_data[slc]
    ], loc="best", frameon=False)

    ax.plot()
    fig.savefig(f"output/{output_folder[i]}/node_routings.png", dpi=300, transparent=False)
    plt.close(fig)
    print("finish")

    # In[ ]:


    fig, ax = plt.subplots(figsize=(8, 4))
    ax.set_title("CDF Alterations per Node")
    ax.set_xlabel("Alterations")
    ax.set_ylabel("Node Percentage")

    for data in tor_data[slc]:
        color = color_dict[data.switch_size]  # Get color based on switch_size
        Plotter.Plotter.cdf_alterations(data.cdf_node_alterations(), ax, color=color)

    ax.legend([
        f"{Plotter.Plotter.get_project_name(data)}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
        for data in tor_data[slc]
    ], loc="best", frameon=False)

    ax.plot()
    fig.savefig(f"output/{output_folder[i]}/node_alterations.png", dpi=300, transparent=False)
    plt.close(fig)
    print("finish")

    # In[ ]:


    fig, ax = plt.subplots(figsize=(8, 4))
    ax.set_title("CDF Routings per Switch")
    ax.set_xlabel("Routing")
    ax.set_ylabel("Switches Percentage")

    for data in tor_data[slc]:
        color = color_dict[data.switch_size]  # Get color based on switch_size
        Plotter.Plotter.cdf_routings(data.cdf_switch_routings(), ax, color=color)

    ax.legend([
        f"{Plotter.Plotter.get_project_name(data)}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
        for data in tor_data[slc]
    ], loc="best", frameon=False)

    ax.plot()
    fig.savefig(f"output/{output_folder[i]}/switch_routings.png", dpi=300, transparent=False)
    plt.close(fig)
    print("finish")

    # In[ ]:


    fig, ax = plt.subplots(figsize=(8, 4))
    ax.set_title("CDF Alterations per Switch")
    ax.set_xlabel("Alterations")
    ax.set_ylabel("Switches Percentage")

    for data in tor_data[slc]:
        color = color_dict[data.switch_size]  # Get color based on switch_size
        Plotter.Plotter.cdf_alterations(data.cdf_switch_alterations(), ax, color=color)

    ax.legend([
        f"{Plotter.Plotter.get_project_name(data)}--{data.dataset}--{data.num_nodes}--{data.num_switches}--{data.switch_size}"
        for data in tor_data[slc]
    ], loc="best", frameon=False)

    ax.plot()
    fig.savefig(f"output/{output_folder[i]}/switch_alterations.png", dpi=300, transparent=False)
    plt.close(fig)
    print("finish")
    '''
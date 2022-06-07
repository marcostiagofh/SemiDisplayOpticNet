import matplotlib.pyplot as plt
import dataclasses as dc
import numpy as np

import statsmodels.api as sm

@dc.dataclass()
class Plotter:
    def cdf_active_switches (
        alterations: np.ndarray, routings: np.ndarray, ax: plt.axes = None
    ) -> None:
        active_switches = (alterations + routings) > 0
        active_percentage = np.sum(active_switches, axis=1).T / 10**4

        ecdf = sm.distributions.empirical_distribution.ECDF(active_percentage)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF Switches ativos por mais que (x) rounds")
            ax.set_xlabel("Rounds x 10^4")
            ax.set_ylabel("Porcentagem dos switches ativos")

        min_indx = max(min(active_percentage - 0.1), 0)
        max_indx = min(max(active_percentage) + 0.1, 10)
        x = np.linspace(min_indx, max_indx)
        y = ecdf(x)

        ax.step(x, y)

    def cdf_active_ports (
        alterations: np.ndarray, routings: np.ndarray, ax: plt.axes = None
    ) -> None:
        active_ports = (alterations + routings).reshape(-1)

        ecdf = sm.distributions.empirical_distribution.ECDF(active_ports)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF Portas ativas por mais que (x) rounds")
            ax.set_xlabel("Rounds")
            ax.set_ylabel("Porcentagem de rounds por portas ativas")

        x = np.linspace(0, max(active_ports) + 100)
        y = ecdf(x)

        ax.step(x, y)

    def cdf_switches_active_ports (
        alterations: np.ndarray, routings: np.ndarray, ax: plt.axes = None
    ) -> None:
        switches_active_ports = (alterations + routings) > 0
        active_percentage = np.sum(switches_active_ports, axis=1).T / switches_active_ports.shape[1]

        ecdf = sm.distributions.empirical_distribution.ECDF(active_percentage)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF % portas ativas por Switch")
            ax.set_xlabel("Porcentagem de portas ativas")
            ax.set_ylabel("Porcentagem dos switches")

        min_indx = max(min(active_percentage - 0.1), 0)
        max_indx = min(max(active_percentage + 0.1), 1)
        x = np.linspace(min_indx, max_indx)
        y = ecdf(x)

        ax.step(x, y)

    def cdf_routings (routs: np.ndarray, ax: plt.axes = None) -> None:
        routs = routs / 10**3
        ecdf = sm.distributions.empirical_distribution.ECDF(routs)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF roteamentos por objeto")
            ax.set_xlabel("Roteamentos x 10^3")
            ax.set_ylabel("Porcentagem dos objetos")

        min_indx = max(min(routs - 10), 0)
        max_indx = max(routs + 10)
        x = np.linspace(min_indx, max_indx)
        y = ecdf(x)

        ax.step(x, y)

    def cdf_alterations   (alterations: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(alterations)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF alterações por objeto")
            ax.set_xlabel("Alterações")
            ax.set_ylabel("Porcentagem dos objetos")

        min_indx = max(min(alterations - 2), 0)
        max_indx = max(alterations + 2)
        x = np.linspace(min_indx, max_indx)
        y = ecdf(x)

        ax.step(x, y)
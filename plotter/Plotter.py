import matplotlib.pyplot as plt
import dataclasses as dc
import numpy as np

import statsmodels.api as sm

plt.style.use(["science","ieee"])

abbr: dict[str, str] = {
    "cbOptNet": "CBN",
    "displayOpticNet": "ODSN",
    "semiDisplayOpticNet": "DSN",
    "HLsemiDisplayOpticNet": "HLDSN",
    "HLSplayOpticNet": "HLSON"
}

@dc.dataclass()
class Plotter:
    @classmethod
    def get_project_name (cls, data) -> str:
        project = abbr[data.project]
        return f"OpticNet({project})"

    @classmethod
    def total_work_link_updates (
        cls, plot_data: list, normalize: int = 1, ax: plt.axes = None
    ) -> None:
        project_names = []
        routing_means = []
        alteration_means = []
        heuristic_creation_means = []
        switch_sizes = []
        work_stds = []

        for data in plot_data:
            project_name = cls.get_project_name(data)

            total_routing, total_link_updates, total_heuristic_creation, total_work = data.read_operations()            

            project_names.append(project_name)
            routing_means.append(total_routing.mean() / normalize)
            alteration_means.append(total_link_updates.mean() / normalize)
            heuristic_creation_means.append(total_heuristic_creation.mean() / normalize)
            work_stds.append((total_work / normalize).std())
            switch_sizes.append(data.switch_size)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("Total Work Link Updates")
            ax.set_xlabel("Project")
            ax.set_ylabel("Work * 10 ^ 4")

        ax.bar(switch_sizes, routing_means, label="Service Cost", color=["silver"])
        ax.bar(
            switch_sizes, alteration_means, 
            bottom=routing_means, label="Link Updates",  color=["grey"]
        )
        ax.bar(
            switch_sizes, heuristic_creation_means, yerr=work_stds,
            bottom=routing_means, label="Heuristic Links",  color=["red"]
        )
        ax.legend(loc="best")
        
    def cdf_active_switches (cdf_array: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF Switches ativos por mais que (x) rounds")
            ax.set_xlabel("Rounds x 10^4")
            ax.set_ylabel("Porcentagem dos switches ativos")

        min_indx = max(min(cdf_array - 0.1), 0)
        x = np.linspace(min_indx, max(cdf_array))
        y = ecdf(x)

        return ax.step(x, y)

    def cdf_active_ports (cdf_array: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF Portas ativas por mais que (x) rounds")
            ax.set_xlabel("Rounds")
            ax.set_ylabel("Porcentagem de rounds por portas ativas")

        x = np.linspace(0, max(cdf_array) + 100)
        y = ecdf(x)

        return ax.step(x, y)

    def cdf_switches_active_ports (cdf_array: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF % portas ativas por Switch")
            ax.set_xlabel("Porcentagem de portas ativas")
            ax.set_ylabel("Porcentagem dos switches")

        min_indx = max(min(cdf_array - 0.1), 0)
        max_indx = max(cdf_array + 0.1)
        x = np.linspace(min_indx, max_indx)
        y = ecdf(x)

        return ax.step(x, y)

    def cdf_routings (cdf_array: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF roteamentos por objeto")
            ax.set_xlabel("Roteamentos x 10^3")
            ax.set_ylabel("Porcentagem dos objetos")

        min_indx = max(min(cdf_array - 10), 0)
        max_indx = max(cdf_array + 10)
        x = np.linspace(min_indx, max_indx)
        y = ecdf(x)

        return ax.step(x, y)

    def cdf_alterations (cdf_array: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF alterações por objeto")
            ax.set_xlabel("Alterações")
            ax.set_ylabel("Porcentagem dos objetos")

        min_indx = max(min(cdf_array - 2), 0)
        max_indx = max(cdf_array + 2)
        x = np.linspace(min_indx, max_indx)
        y = ecdf(x)

        return ax.step(x, y)
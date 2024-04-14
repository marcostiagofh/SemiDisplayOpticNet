import matplotlib.pyplot as plt
import dataclasses as dc
import numpy as np

import statsmodels.api as sm

plt.style.use(["science","ieee"])

abbr: dict[str, str] = {
    "cbOptNet": "CBN",
    "cbOptNetHL": "CBN",
    "displayOpticNet": "ODSN",
    "semiDisplayOpticNet": "DSN",
    "semiDisplayOpticNetHL" : "DSN",
    "semiDisplayOpticNetHLAP": "DSN",
    "SplayOpticNet": "SN"
}

@dc.dataclass()
class Plotter:
    @classmethod
    def get_project_name (cls, data) -> str:
        project = abbr[data.project]
        if data.project == "semiDisplayOpticNetHL":
            return "OpticNet$^{OP}$" + f"({project})"
        elif data.project == "semiDisplayOpticNetHLAP":
            return "OpticNet$^{OP AP}$" + f"({project})"
        elif data.project == "cbOptNetHL":
            return "OpticNet$^{OP}$" + f"({project})"
        else:
            return f"OpticNet({project})"
            
    @classmethod
    def total_rounds (
        cls, plot_data: list, normalize: int = 1, ax: plt.axes = None
    ) -> None:
        project_names = []
        all_datasets_rounds = []

        for data in plot_data:
            project_name = cls.get_project_name(data)

            rounds = data.read_rounds()            

            project_names.append(project_name)
            all_datasets_rounds.append(rounds.mean())

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("Total Work")
            ax.set_xlabel("Project")
            ax.set_ylabel("Work * 10 ^ 4")

        ax.bar(
            project_names, all_datasets_rounds,
            color=["grey"]
        )
        ax.legend(loc="best")

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

            total_routing, total_alterations, total_work = data.read_operations()            

            project_names.append(project_name)
            routing_means.append(total_routing.mean() / normalize)
            alteration_means.append(total_alterations.mean() / normalize)
            work_stds.append((total_work / normalize).std())
            switch_sizes.append(data.switch_size)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("Total Work")
            ax.set_xlabel("Project")
            ax.set_ylabel("Work * 10 ^ 4")

        ax.bar(
            project_names, alteration_means, 
            bottom=routing_means, label="Link Updates",  color=["grey"]
        )
        ax.bar(project_names, routing_means, label="Service Cost", color=["silver"])
        ax.legend(loc="best")
        
    def cdf_active_switches (cdf_array: np.ndarray, ax: plt.axes = None, color: tuple = None) -> None:
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

        if color is None:
            return ax.step(x, y)
        else:
            return ax.step(x, y, color=color)

    def cdf_active_ports (cdf_array: np.ndarray, ax: plt.axes = None, color: tuple = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF Portas ativas por mais que (x) rounds")
            ax.set_xlabel("Rounds")
            ax.set_ylabel("Porcentagem de rounds por portas ativas")

        x = np.linspace(0, max(cdf_array) + 100)
        y = ecdf(x)

        if color is None:
            return ax.step(x, y)
        else:
            return ax.step(x, y, color=color)

    def cdf_switches_active_ports (cdf_array: np.ndarray, ax: plt.axes = None, color: tuple = None) -> None:
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
        
        if color is None:
            return ax.step(x, y)
        else:
            return ax.step(x, y, color=color)


    def cdf_routings (cdf_array: np.ndarray, ax: plt.axes = None, color: tuple = None) -> None:
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

        if color is None:
            return ax.step(x, y)
        else:
            return ax.step(x, y, color=color)

    def cdf_alterations (cdf_array: np.ndarray, ax: plt.axes = None, color: tuple = None) -> None:
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

        if color is None:
            return ax.step(x, y)
        else:
            return ax.step(x, y, color=color)
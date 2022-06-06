import dataclasses as dc
import pandas as pd
import numpy as np

from pathlib import Path

@dc.dataclass(init=True)
class DataReader:
    dataset: str = dc.field(init=True)
    project: str = dc.field(init=True)
    num_nodes: int = dc.field(init=True)
    switch_size: int = dc.field(init=True)
    num_sims: int = dc.field(init=True)
    switch_ports: int = dc.field(init=False)
    num_switches_1: int = dc.field(init=False)
    num_switches_2: int = dc.field(init=False)
    max_rounds: int = dc.field(init=False)

    def __post_init__ (self) -> None:
        self.max_rounds = -1
        for sim_id in range(1, self.num_sims + 1):
            with open(self.file_path / f"{sim_id}/simulation_info.txt") as sim_file:
                self.max_rounds = max(self.max_rounds, int(sim_file.readline().split(",")[1]))

        with open(self.file_path / f"{sim_id}/simulation_info.txt") as sim_file:
            sim_file.readline()

            _, clusters_1, _, c1_size = sim_file.readline().split(",")
            _, clusters_2, _, c2_size = sim_file.readline().split(",")
            clusters_1, c1_size, clusters_2, c2_size = map(
                int, (clusters_1, c1_size, clusters_2, c2_size)
            )

            self.num_switches_1 = clusters_1 * c1_size
            self.num_switches_2 = clusters_2 * c2_size

        self.switch_ports = self.switch_size // 2

    @property
    def num_switches (self) -> int:
        return self.num_switches_1 + self.num_switches_2

    @property
    def file_path (self) -> Path:
        return Path(
            Path(__file__).parent.parent /
            f"logs/output/{self.dataset}/{self.project}_{self.num_nodes}/{self.switch_size}"
        )

    def read_operations (self) -> tuple[np.ndarray, np.ndarray, np.ndarray]:
        total_routing = np.empty(self.num_sims)
        total_alterations = np.empty(self.num_sims)

        for sim_id in range(1, self.num_sims + 1):
            file_df = pd.read_csv(self.file_path / f"{sim_id}/operations.csv")

            total_routing[sim_id - 1] = file_df.loc[file_df.name=="message-routing", "sum"].item()
            total_alterations[sim_id - 1] = file_df.loc[file_df.name=="alteration", "sum"].item()

        total_work = total_routing + total_alterations

        return total_routing, total_alterations, total_work

    def read_throughput (self) -> np.ndarray:
        raw = np.zeros((self.num_sims, self.max_rounds), dtype=np.ndarray)

        for sim_id in range(1, self.num_sims + 1):
            file_df = pd.read_csv(self.file_path / f"{sim_id}/throughput.csv")

            for _, row in file_df.iterrows():
                raw[row["round"]] += row["completed_requests"]
                raw[row["round"]] += row["completed_requests"]

        raw = np.array([ np.pad(line, (0, self.max_rounds - len(line))) for line in raw ])
        completed_requests = np.sum(raw, axis=0) / self.num_sims

        return completed_requests

    def read_active_requests (self) -> np.ndarray:
        raw = np.zeros((self.num_sims, self.max_rounds), dtype=np.ndarray)

        for sim_id in range(1, self.num_sims + 1):
            file_df = pd.read_csv(self.file_path / f"{sim_id}/active_requests_per_round.csv")
            np_raw = file_df["active_requests"].to_numpy()

            raw[sim_id - 1, : len(np_raw) ] = np_raw

        active_requests = np.sum(raw, axis=0) / self.num_sims
        return active_requests

    def read_total_alterations (
        self, alterations: bool = False, nodes_alterations: bool = False,
        switches_alterations: bool = False, altered_ports: bool = False, sims: int = None
    ) -> list[np.ndarray]:
        sims = list(range(1, self.num_sims + 1)) if sims is None else [ sims ]
        alt_round = np.zeros(self.max_rounds) if alterations else None
        alt_node = np.zeros(self.num_nodes) if nodes_alterations else None
        alt_switch = np.zeros(self.num_switches) if switches_alterations else None
        alt_ports = np.zeros((self.num_switches, self.switch_ports)) if altered_ports else None

        for sim_id in sims:
            file_df = pd.read_csv(self.file_path / f"{sim_id}/alterations_per_round.csv")

            for _, row in file_df.iterrows():
                if alterations:
                    alt_round[row["round"]] += 1

                if nodes_alterations:
                    alt_node[row["node"]] += 1

                if switches_alterations:
                    alt_switch[row["switch"]] += 1

                if altered_ports:
                    alt_ports[ row["switch"], row["node"] % self.switch_ports ] += 1

        ret = []
        ret.append(alt_ports / len(sims)) if altered_ports else None
        ret.append(alt_round / (len(sims) * 2)) if alterations else None
        ret.append(alt_node / len(sims)) if nodes_alterations else None
        ret.append(alt_switch / len(sims)) if switches_alterations else None

        return ret

    def read_total_routings (
        self, routings: bool = False, nodes_routings: bool = False, switches_routings: bool = False,
        routing_ports: bool = False, sims: int = None
    ) -> list[np.ndarray]:
        sims = list(range(1, self.num_sims + 1)) if sims is None else [ sims ]
        rout_round = np.zeros(self.max_rounds) if routings else []
        rout_node = np.zeros(self.num_nodes) if nodes_routings else []
        rout_switch = np.zeros(self.num_switches) if switches_routings else []
        rout_ports = np.zeros((self.num_switches, self.switch_ports)) if routing_ports else None

        for sim_id in sims:
            file_df = pd.read_csv(self.file_path / f"{sim_id}/routings_per_round.csv")

            for _, row in file_df.iterrows():
                if routings:
                    rout_round[row["round"]] += 1

                if nodes_routings:
                    rout_node[row["from_node"]] += 1
                    rout_node[row["to_node"]] += 1

                if switches_routings:
                    rout_switch[row["switch"]] += 1

                if routing_ports:
                    rout_ports[ row["switch"], row["from_node"] % self.switch_ports ] += 1

        ret = []
        ret.append(rout_round / len(sims)) if routings else None
        ret.append(rout_ports / len(sims)) if routing_ports else None
        ret.append(rout_node / len(sims)) if nodes_routings else None
        ret.append(rout_switch / len(sims)) if switches_routings else None

        return ret

    def read_altered_ports (self, sims: int = None) -> list[np.ndarray]:
        sims = list(range(1, self.num_sims + 1)) if sims is None else [ sims ]
        alt_switch = np.zeros((self.num_switches, self.switch_ports))

        for sim_id in sims:
            file_df = pd.read_csv(self.file_path / f"{sim_id}/alterations_per_round.csv")
            ports_df = file_df.drop_duplicates(subset=["node", "switch"])

            for _, row in ports_df.iterrows():
                alt_switch[ row["switch"], row["node"] % self.switch_ports ] += 1

        return alt_switch / len(sims)

    def read_routing_ports (self, sims: int = None) -> np.ndarray:
        sims = list(range(1, self.num_sims + 1)) if sims is None else [ sims ]
        rout_switch = np.zeros((self.num_switches, self.switch_ports))

        for sim_id in sims:
            file_df = pd.read_csv(self.file_path / f"{sim_id}/routings_per_round.csv")
            ports_df = file_df.drop_duplicates(subset=["from_node", "switch"])

            for _, row in ports_df.iterrows():
                rout_switch[row["switch"], row["from_node"] % self.switch_ports ] += 1

        return rout_switch / len(sims)

    def read_routings (
        self, nodes_routings: bool = False, switches_routings: bool = False, sims: int = None
    ) -> list[np.ndarray]:
        sims = list(range(1, self.num_sims + 1)) if sims is None else [ sims ]
        rout_node = np.zeros((self.num_nodes, self.max_rounds)) if nodes_routings else []
        rout_switch = np.zeros((self.num_switches, self.max_rounds)) if switches_routings else []

        for sim_id in sims:
            file_df = pd.read_csv(self.file_path / f"{sim_id}/routings_per_round.csv")

            for _, row in file_df.iterrows():
                if nodes_routings:
                    rout_node[row["from_node"], row["round"]] += 1
                    rout_node[row["to_node"], row["round"]] += 1

                if switches_routings:
                    rout_switch[row["switch"], row["round"]] += 1

        ret = []
        ret.append(rout_node / len(sims)) if nodes_routings else None
        ret.append(rout_switch / len(sims)) if switches_routings else None

        return ret

    def read_alterations (
        self, nodes_alterations: bool = False, switches_alterations: bool = False, sims: int = None
    ) -> list[np.ndarray]:
        sims = list(range(1, self.num_sims + 1)) if sims is None else [ sims ]
        alt_node = np.zeros((self.num_nodes, self.max_rounds)) if nodes_alterations else None
        alt_switch = np.zeros((self.num_switches, self.max_rounds)) if switches_alterations else None

        for sim_id in sims:
            file_df = pd.read_csv(self.file_path / f"{sim_id}/alterations_per_round.csv")

            for _, row in file_df.iterrows():
                if nodes_alterations:
                    alt_node[row["node"], row["round"]] += 1

                if switches_alterations:
                    alt_switch[row["switch"], row["round"]] += 1

        ret = []
        ret.append(alt_node / len(sims)) if nodes_alterations else None
        ret.append(alt_switch / (len(sims) * 2)) if switches_alterations else None

        return ret

    def read_active_ports (self, sims: int = None) -> None:
        sims = list(range(1, self.num_sims + 1)) if sims is None else [ sims ]
        active_ports = np.zeros((self.num_switches, self.max_rounds))

        for sim_id in sims:
            file_df = pd.read_csv(self.file_path / f"{sim_id}/switches_active_ports_per_round.csv")

            for _, row in file_df.iterrows():
                active_ports[row["switch"], row["round"]] += row["active_ports"]
                active_ports[row["switch"] + 1, row["round"]] += row["active_ports"]

        for switch in active_ports:
            for i in range(1, self.max_rounds):
                switch[i] += switch[i - 1]

        return active_ports / len(sims)

setwd("C:\Users\marco\Downloads\SemiDisplayOpticNet")

################################## Libraries ###################################

library(ggplot2)
library(gridExtra)
library(tidyverse)
library(plyr)

# setup
options(scipen = 999)
theme_set(theme_bw())

############################# Reading tables  ##################################

throughput.table <- read.csv(".\logs\output\bursty-0.4-1\semiDisplayOpticNetHL_128\256\4\1\throughput.csv")

############################# Define colors  ##################################

#COLORS
#cbn_color = "#325387"
cbn_color = "#000000"
cbn1 = "#325387"
cbn2 = "#325387"

#scbn_color = "#6C5B7B"
scbn_color = "#ffffff"
scbn1 = "#6C5B7B"
scbn2 = "#6C5B7B"

#sn_color = "#021C02"
sn_color = "#A8A7A7"
sn1 = "#1A361A"
sn2 = "#021C02"

#dsn_color = "#B53131"
dsn_color = "#2A363B"
dsn1 = "#CF4F4F"
dsn2 = "#B53131"

opt_color = "#87C8E6"
opt1 = "#BEDCEB"
opt2 = "#87C8E6"

bt_color = "#555555"
bt1 = "#555555"
bt2 = "#555555"

############################# Define imgs paremeters ######################

scale_imgs <- 1

IMG_height = 15
IMG_width = 20

text_size <- 30
x_title_size <- 25
y_title_size <- 25
x_text_size <- 25
y_text_size <- 25

num_sim <- 30


############################# throughput  ##################################


throughput.table["abb"] <- revalue(throughput.table$project, 
                                   c("cbnet" = "CBN",
                                     "cbnetAdapt" = "AD",
                                     "seqcbnet" = "SCBN",
                                     "splaynet" = "SN", 
                                     "displaynet" = "DSN"))

throughput.table$abb <- factor(throughput.table$abb, levels = c("CBN", "AD","DSN", "SN", "SCBN"))


throughput.table %>% filter(
  size %in% c(1024)) -> throughput.table

# Init Ggplot Base Plot
throughput.plot <- ggplot(throughput.table, aes(x = value, fill = abb)) +
  geom_density(aes(y = ..count..), alpha = 0.67) 

# Modify theme components -------------------------------------------
throughput.plot <- throughput.plot + theme(text = element_text(size = text_size),
                                           plot.title = element_blank(),
                                           plot.subtitle = element_blank(),
                                           plot.caption = element_blank(),
                                           axis.title.x = element_text(size = x_title_size),
                                           axis.title.y = element_text(size = y_title_size),
                                           axis.text.x = element_text(size = x_text_size, color = "black"),
                                           axis.text.y = element_text(size = y_text_size, color = "black"),
                                           #legend.text = element_text(size = text_size),
                                           legend.title = element_blank(),
                                           legend.position = c(0.82, 0.77)) #+
  #facet_grid(. ~ size)

throughput.plot <- throughput.plot + theme(panel.grid.minor = element_blank(),
                                           panel.grid.major = element_blank()) +
  labs(x = expression(paste("Time (rounds) x", 10^4)), y = "Requests completed/round") +
  scale_fill_manual(values = c(cbn_color, bt_color, dsn_color, sn_color, scbn_color)) +
  scale_y_continuous(lim = c(0, 0.8), breaks = seq(0, 5, 0.1)) +
  scale_x_continuous(labels = function(x){paste0(x/10000)})

plot(throughput.plot)

ggsave(filename = "./plots/bursty/throughput.png", units = "cm",
       plot = throughput.plot, device = "png",  width = IMG_width, height = IMG_height, scale = scale_imgs)

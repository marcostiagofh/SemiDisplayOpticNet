setwd("C:\\Users\\marco\\Downloads\\SemiDisplayOpticNet")

################################## Libraries ###################################

library(ggplot2)
library(gridExtra)
library(tidyverse)
library(plyr)

# setup
options(scipen = 999)
theme_set(theme_bw())

############################# Define colors  ##################################

#COLORS
#cbn_color = "#325387"
cbn_color = "#000000"
cbn1 = "#325387"
cbn2 = "#325387"

#scbn_color = "#6C5B7B"
cbnhl_color = "#ffffff"
cbnhl = "#6C5B7B"
cbnhl = "#6C5B7B"

#dsn_color = "#B53131"
dsn_color = "#2A363B"
dsn1 = "#CF4F4F"
dsn2 = "#B53131"

dsnhl_color = "#87C8E6"
dsnhl1 = "#BEDCEB"
dsnhl2 = "#87C8E6"

dsnhlap_color = "#555555"
dsnhlap1 = "#555555"
dsnhlap2 = "#555555"

############################# Define imgs paremeters ######################

scale_imgs <- 1

IMG_height = 15
IMG_width = 20

text_size <- 30
x_title_size <- 25
y_title_size <- 25
x_text_size <- 25
y_text_size <- 25

num_sim <- 1


############################# Reading tables  ##################################

throughput.table <- read.csv(".\\csv_data\\hpcDS-exact_boxlib_cns_nospec_large\\throughput.csv")

############################# throughput  ##################################


throughput.table["abb"] <- revalue(throughput.table$project, 
                                   c("CBOpticalNet-master" = "CBN",
                                     "CBOpticalNet" = "CBNHL",
                                     "SemiDisplayOpticNet-master" = "DSN",
                                     "SemiDisplayOpticNet" = "DSNHL", 
                                     "SemiDisplayOpticNet-AP" = "DSNHLAP"))

throughput.table$abb <- factor(throughput.table$abb, levels = c("CBN", "CBNHL", "DSN", "DSNHL", "DSNHLAP"))


#throughput.table %>% filter(
#  size %in% c(100)) -> throughput.table

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
  scale_fill_manual(values = c(cbn_color, cbnhl_color, dsn_color, dsnhl_color, dsnhlap_color)) +
  scale_y_continuous(lim = c(0, 1.0), breaks = seq(0, 5, 0.1)) +
  scale_x_continuous(labels = function(x){paste0(x/10000)})

plot(throughput.plot)

ggsave(filename = "./output/hpcDS-exact_boxlib_cns_nospec_large/throughput.png", units = "cm",
       plot = throughput.plot, device = "png",  width = IMG_width, height = IMG_height, scale = scale_imgs)

# PersonalizedPageRank
A Java implementation of the Personalized PageRank algorithm



# SimRank

This repository contains the source code implementation of Personalized PageRank and a dataset for testing the algorithm.


## Introduction


This is a Java source code implementation of the following paper:

**Topic-sensitive PageRank** ([link](http://www-cs-students.stanford.edu/~taherh/papers/topic-sensitive-pagerank.pdf)).

We exploited sparse matrix to optimize the memory usage and thus increasing the computational performance. There is already an example embedded so that you can test the implementation. We represented a graph for GitHub repositories using various information as it can be seen in the following figure.

<p align="center">
<img src="https://github.com/phuongthanhnguyen/SimRank/blob/master/Data/Graph.png" width="450">
</p> 


By exploiting this graph, we are able to compute the similarity between any pair of GitHub repositories.

By executing the code, you are able to compute the similarity between any pair of nodes in the graph.

## How to cite
If you find the tool useful for your work, please cite it using the following BibTex entries:

```
@inproceedings{Nguyen:2015:ESP:2740908.2742141,
 author = {Nguyen, Phuong and Tomeo, Paolo and Di Noia, Tommaso and Di Sciascio, Eugenio},
 title = {An Evaluation of SimRank and Personalized PageRank to Build a Recommender System for the Web of Data},
 booktitle = {Proceedings of the 24th International Conference on World Wide Web},
 series = {WWW '15 Companion},
 year = {2015},
 isbn = {978-1-4503-3473-0},
 location = {Florence, Italy},
 pages = {1477--1482},
 numpages = {6},
 url = {http://doi.acm.org/10.1145/2740908.2742141},
 doi = {10.1145/2740908.2742141},
 acmid = {2742141},
 publisher = {ACM},
 address = {New York, NY, USA},
 keywords = {personalized pagerank, recommender systems, simrank, web of data},
} 
```


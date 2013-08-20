/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package peersim.graph;

import java.util.*;

/**
 * Contains static methods for wiring certain kinds of graphs. The general
 * contract of all methods is that they accept any graph and add edges
 * as specified in the documentation.
 */
public class GraphFactory {
    
    /** Disable instance construction */
    private GraphFactory() {}
    
// ===================== public static methods ======================
// ==================================================================
    
    /**
     * Wires a ring lattice.
     * The added connections are defined as follows. If k is even, links to
     * i-k/2, i-k/2+1, ..., i+k/2 are added (but not to i), thus adding an
     * equal number of predecessors and successors.
     * If k is odd, then we add one more successors than predecessors.
     * For example, for k=4: 2 predecessors, 2 successors.
     * For k=5: 2 predecessors, 3 successors.
     * For k=1: each node is linked only to its successor.
     * All values are understood mod n to make the lattice circular, where n is the
     * number of nodes in g.
     * @param g the graph to be wired
     * @param k lattice parameter
     * @return returns g for convenience
     */
    public static Graph wireRingLattice(Graph g, int k) {
        
        final int n = g.size();
        
        int pred = k/2;
        int succ = k-pred;
        
        for(int i=0; i<n; ++i)
            for(int j=-pred; j<=succ; ++j) {
            if( j==0 ) continue;
            final int v = (i+j+n)%n;
            g.setEdge(i,v);
            }
        return g;
    }
    
// -------------------------------------------------------------------
    
    /**
     * Watts-Strogatz model. A bit modified though: by default assumes a directed
     * graph. This means that directed
     * links are re-wired, and the undirected edges in the original (undirected)
     * lattice are modeled
     * by double directed links pointing in opposite directions. Rewiring is done
     * with replacement, so the possibility of wiring two links to the same target
     * is positive (though very small).
     * <p>
     * Note that it is possible to pass an undirected graph as a parameter. In that
     * case the output is the directed graph produced by the method, converted to
     * an undirected graph by dropping directionality of the edges. This graph is
     * still not from the original undirected WS model though.
     * @param g the graph to be wired
     * @param k lattice parameter: this is the out-degree of a node in the
     * ring lattice before rewiring
     * @param p the probability of rewiring each
     * @param r source of randomness
     * @return returns g for convenience
     */
    public static Graph wireWS( Graph g, int k, double p, Random r ) {
//XXX unintuitive to call it WS due to the slight mods
        final int n = g.size();
        for(int i=0; i<n; ++i)
            for(int j=-k/2; j<=k/2; ++j) {
            if( j==0 ) continue;
            int newedge = (i+j+n)%n;
            if( r.nextDouble() < p ) {
                newedge = r.nextInt(n-1);
                if( newedge >= i ) newedge++; // random _other_ node
            }
            g.setEdge(i,newedge);
            }
        return g;
    }
    
// -------------------------------------------------------------------
    
    /**
     * Random graph. Generates randomly k directed edges out of each node.
     * The neighbors
     * (edge targets) are chosen randomly without replacement from the nodes of the
     * graph other than the source node (i.e. no loop edge is added).
     * If k is larger than N-1 (where N is the number of nodes) then k is set to
     * be N-1 and a complete graph is returned.
     * @param g the graph to be wired
     * @param k samples to be drawn for each node
     * @param r source of randomness
     * @return returns g for convenience
     */
    public static Graph wireKOut( Graph g, int k, Random r ) {
        
        final int n = g.size();
        if( n < 2 ) return g;
        if( n <= k ) k=n-1;
        int[] nodes = new int[n];
        for(int i=0; i<nodes.length; ++i) nodes[i]=i;
        for(int i=0; i<n; ++i) {
            int j=0;
            while(j<k) {
                int newedge = j+r.nextInt(n-j);
                int tmp = nodes[j];
                nodes[j] = nodes[newedge];
                nodes[newedge] = tmp;
                if( nodes[j] != i ) {
                    g.setEdge(i,nodes[j]);
                    j++;
                }
            }
        }
        return g;
    }
    
// -------------------------------------------------------------------
    
    /**
     * A sink star.
     * Wires a sink star topology adding a link to 0 from all other nodes.
     * @param g the graph to be wired
     * @return returns g for convenience
     */
    public static Graph wireStar( Graph g ) {
        
        final int n = g.size();
        for(int i=1; i<n; ++i) g.setEdge(i,0);
        return g;
    }
    
// -------------------------------------------------------------------
    
    /**
     * A regular rooted tree.
     * Wires a regular rooted tree. The root is 0, it has links to 1,...,k.
     * In general, node i has links to i*k+1,...,i*k+k.
     * @param g the graph to be wired
     * @param k the number of outgoing links of nodes in the tree (except
     * leaves that have zero out-links, and exactly one node that might have
     * less than k).
     * @return returns g for convenience
     */
    public static Graph wireRegRootedTree( Graph g, int k ) {
        
        if( k==0 ) return g;
        final int n = g.size();
        int i=0; // node we wire
        int j=1; // next free node to link to
        while(j<n) {
            for(int l=0; l<k && j<n; ++l,++j) g.setEdge(i,j);
            ++i;
        }
        return g;
    }
    
// -------------------------------------------------------------------
    
    /**
     * A hypercube.
     * Wires a hypercube.
     * For a node i the following links are added: i xor 2^0, i xor 2^1, etc.
     * this define a log(graphsize) dimensional hypercube (if the log is an
     * integer).
     * @param g the graph to be wired
     * @return returns g for convenience
     */
    public static Graph wireHypercube( Graph g ) {
        
        final int n = g.size();
        if(n<=1) return g;
        final int highestone = Integer.highestOneBit(n-1); // not zero
        for(int i=0; i<n; ++i) {
            int mask = highestone;
            while(mask>0) {
                int j = i^mask;
                if(j<n) g.setEdge(i,j);
                mask = mask >> 1;
            }
            
        }
        return g;
    }
    
// -------------------------------------------------------------------
    
    /**
     * This contains the implementation of the Barabasi-Albert model
     * of growing scale free networks. The original model is described in
     * <a href="http://arxiv.org/abs/cond-mat/0106096">
http://arxiv.org/abs/cond-mat/0106096</a>.
     * It also works if the graph is directed, in which case the model is a
     * variation of the BA model
     * described in <a href="http://arxiv.org/pdf/cond-mat/0408391">
http://arxiv.org/pdf/cond-mat/0408391</a>. In both cases, the number of the
     * initial set of nodes is the same as the degree parameter, and no links are
     * added. The first added node is connected to all of the initial nodes,
     * and after that the BA model is used normally.
     * @param k the number of edges that are generated for each new node, also
     * the number of initial nodes (that have no edges).
     * @param r the randomness to be used
     * @return returns g for convenience
     */
    public static Graph wireScaleFreeBA( Graph g, int k, Random r ) {
        
        final int nodes = g.size();
        if( nodes <= k ) return g;
        
        // edge i has ends (ends[2*i],ends[2*i+1])
        int[] ends = new int[2*k*(nodes-k)];
        
        // Add initial edges from k to 0,1,...,k-1
        for(int i=0; i < k; i++) {
            g.setEdge(k,i);
            ends[2*i]=k;
            ends[2*i+1]=i;
        }
        
        int len = 2*k; // edges drawn so far is len/2
        for(int i=k+1; i < nodes; i++) // over the remaining nodes
        {
            for (int j=0; j < k; j++) // over the new edges
            {
                int target;
                do
                {
                    target = ends[r.nextInt(len)];
                    int m=0;
                    while( m<j && ends[len+2*m+1]!=target) ++m;
                    if(m==j) break;
                    // we don't check in the graph because
                    // this wire method should accept graphs
                    // that already have edges.
                }
                while(true);
                g.setEdge(i,target);
                ends[len+2*j]=i;
                ends[len+2*j+1]=target;
            }
            len += 2*k;
        }
        
        return g;
    }
        
    public static Graph wireKOutUnd(Graph g, int k, Random r) {
        int debug = 0;

  // -------------------------------------------------------------------
  /**
   * Random graph. Generates randomly k-connected graph, where each node has exactly k-egdes, while
   * one node may have k+1 or -1 edges.
   * The neighbors (edge targets) are chosen randomly from the nodes of the
   * graph other than the source node (i.e. no loop edge is added).
   * In the first phase the algorithm tries to reach a K-connected graph,
   * leaving some node with less edges than .
   * In the second phase, it replaces several links among nodes, to fill
   * the gaps in those nodes with less than k edges, leading to a
   * k-connected graph.
   * @param g the graph to be wired
   * @param k samples to be drawn for each node
   * @param r source of randomness
   * @return returns g for convenience
   */
  public static Graph wireKOutUnd(Graph g, int k, Random r) {
    int debug = 0;//GIVE ME A POSITIVE VALUE >0 & < 10 TO SEE A LOT OF DEBUG MESSAGES.
    if (debug > 8) {
      System.out.println("Nodes " + g.size() + " links " + k);
    }
    final int n = g.size();    
    if (n < 2) {
      return g;
    }    
    if (n <= k) {
      k = n - 1;
    }
    int[][] matrix = new int[n][n];
    //init adjacency matrix
    for (int i = 0; i < n; ++i) {
      for (int j = 0; j < n; ++j) {
        matrix[i][j] = 0;
      }
    }
    if (debug > 8) {
      System.out.println("Adjacency matrix initilized and empty. " + n);
    }
    //matrix used to store links for permutation
    int nodes[] = new int[n];
    for (int i = 0; i < n; ++i) {
      nodes[i] = i;
    }
    //index permutation
    for (int i = 0; i < n; ++i) {
      int j = r.nextInt(n);
      int tmp = nodes[i];
      nodes[i] = nodes[j];
      nodes[j] = tmp;
    }
    for (int current = 0; current < n; current++) {
      int curr_links = 0;
      if (debug > 8) {
        System.out.print("\nNode " + current + " > |");
      }
      //check number of edges of this node
      for (int c = 0; c < n; c++) {
        if (debug > 8) {
          System.out.print(matrix[current][c] + "|");
        }
        if (matrix[current][c] == 1) {
          curr_links++;
        }
      }
      if (debug > 8) {
        System.out.println("< # " + curr_links + ", looking for " + (k - curr_links) + " links.");
      }
      int j = n;//available nodes for set.
      //removing current node from its candidate SET
      for (int i = 0; i < n; i++) {
        if (nodes[i] == current) {
          int tmp = nodes[i];
          nodes[i] = nodes[n - 1];
          nodes[n - 1] = tmp;
          j--;
          i = n;
        }
      }
      if (debug > 8) {
        System.out.println("Start while for " + current + ", size " + j + " escluding " + nodes[j]);
      }
      //First phase: creates as many symmetric edges as possible.
      while (curr_links < k && j > 0) {
        int candidate_id = r.nextInt(j);//pick a node
        int cnd_link = 0;//
        for (int c = 0; c < n; c++) {//count actual number of edges
          if (matrix[nodes[candidate_id]][c] == 1) {
            cnd_link++;
          }
        }
        if (debug > 8) {
          System.out.println("Candidate " + nodes[candidate_id] + " (" + candidate_id + ")");
        }
        if (debug > 8) {
          System.out.println((cnd_link < k) + "  AND " + (j > 0));
        }
        int split = -1;
        
        while (curr_links < k && j > 0) {
          // Current node is ALFA, the target node is called BETA.
          // If there is already a link between ALFA & BETA or if ALFA and BETA are the same node
          // OR if the number of edges of BETA is equal to k
          // OR there are no useful node to connect to ALFA (e.g., all nodes has already k-edges)
          if (debug > 8) {
            System.out.println("\nCandidate set " + j + " nodes. Candidate is " + nodes[candidate_id] + " with " + cnd_link
                    + "links.\n\tMatrix[" + current + "][" + nodes[candidate_id] + "] ?=? " + matrix[current][nodes[candidate_id]]);
          }
          if (debug > 8) {
            System.out.print("\tCandidate needs links: " + (cnd_link < k));
            System.out.println(" AND More candidates: " + (j > 1));
          }
          //removing a link from candidate
          if (cnd_link == k && (curr_links + 2) <= k && split > 0
                  && matrix[nodes[candidate_id]][current] != 1 && matrix[current][nodes[candidate_id]] != 1
                  && matrix[current][split] != 1 && matrix[split][current] != 1
                  && matrix[nodes[candidate_id]][split] == 1 && matrix[split][nodes[candidate_id]] == 1) {
            if (debug > 8) {
              System.out.println("\tSplit on " + split + ". Current links " + curr_links + ", Cnd links " + cnd_link + " KK " + k);
              System.out.println("\tMatrix " + matrix[split][nodes[candidate_id]] + " - " + matrix[nodes[candidate_id]][split]);
            }
            matrix[nodes[candidate_id]][split] = matrix[split][nodes[candidate_id]] = 0;
            matrix[nodes[candidate_id]][current] = matrix[current][nodes[candidate_id]] = 1;
            curr_links++;
            matrix[current][split] = matrix[split][current] = 1;
            curr_links++;
            if (debug > 8) {
              int cul = 0;
              int cil = 0;
              int spl = 0;
              for (int count = 0; count < matrix[current].length; count++) {
                if (matrix[nodes[candidate_id]][count] == 1) {
                  cul++;
                }
                if (matrix[current][count] == 1) {
                  cil++;
                }
                if (matrix[split][count] == 1) {
                  spl++;
                }
              }
              System.out.println("Curr " + cil + (cil > k ? "##" : "") + ", Cand " + cul + (cul > k ? "##" : "") + ", Spl " + spl + (spl > k ? "##" : ""));
            }
          } else if (curr_links < k && cnd_link < k) {
            if (nodes[candidate_id] != current) {
              matrix[current][nodes[candidate_id]] = 1;
              matrix[nodes[candidate_id]][current] = 1;
              curr_links++;
              if (debug > 8) {
                System.out.println("\tLink DONE [" + current + "][" + nodes[candidate_id] + "]=" + matrix[current][nodes[candidate_id]] + ", set is " + j);
              }
            }
          }

          //count current node links
          if (debug > 8) {
            int _curr_links = 0;
            System.out.print("Node " + current + " >");
            for (int c = 0; c < n; c++) {
              System.out.print(matrix[current][c] + "|");
              if (matrix[current][c] == 1) {
                _curr_links++;
              }
            }
            System.out.println("< # " + _curr_links);
          }
          //swap nodes
          if (j > 0) {
            if (debug > 8) {
              System.out.println("\tnodes[" + candidate_id + "]=" + nodes[candidate_id] + " <=> nodes[" + j + "]=" + nodes[j]);
            }
            int tmp = nodes[j];
            nodes[j] = nodes[candidate_id];
            nodes[candidate_id] = tmp;
            if (debug > 8) {
              System.out.print("\tExcluding " + nodes[j] + " size " + j);
            }
          }
          j--;
          if (debug > 8) {
            System.out.println(" --> " + j);
          }
          if (j > 0) {
            candidate_id = r.nextInt(j + 1);
            if (split >= 0) {
              split = -1;
            }
            cnd_link = 0;
            for (int c = 0; c < n; c++) {//conto quanti curr_links ha il nodo BETA
              if (matrix[nodes[candidate_id]][c] == 1) {
                cnd_link++;
                if (cnd_link > 0 && split < 0 && matrix[current][nodes[candidate_id]] != 1 && matrix[current][c] != 1) {
                  split = c;
                }
              }
            }
          }

        }
        //END WHILE
        //SECOND PHASE

        if (debug > 8) {
          System.out.print("\t " + nodes[j] + " size " + j);
          System.out.print("\nNode " + current + " <|");
          int _curr_links = 0;
          for (int c = 0; c < n; c++) {
            System.out.print(matrix[current][c] + "|");
            if (matrix[current][c] == 1) {
              _curr_links++;
            }
          }
          System.out.println("< # " + _curr_links);
        }
      }
      if (debug > 4) {
        System.out.print("Node " + current);
        for (int z = 0; z < matrix[current].length; z++) {
          if (matrix[current][z] == 1) {
            System.out.print(" " + z + " (" + matrix[z][current] + ");");
          }
        }
        System.out.println(".");
      }

    }
    int part = 0;
    int cnt = 0;
    int err = 0;
    for (int i = 0; i < n; i++) {
      if (debug >= 4) {
        System.out.print("Node " + i + ": ");
      }
      for (int j = 0; j < n; j++) {
        if (matrix[i][j] == 1) {
          if (i == j || matrix[j][i] != 1) {
            if (debug > 3) {
              System.out.println("??ERRORE!!");
            }
            part++;
          } else {
            cnt++;
            if (debug > 3) {
              System.out.print(j + " ");
            }
            g.setEdge(i, j);
            g.setEdge(j, i);
          }
        }
//        else if (debug > 3) {
//          System.out.print(0 + " ");
//        }
      }
      if (debug > 3) {
        System.out.println(" >> " + cnt + (cnt != k ? "!!! needs " + err + " links " : ""));//"] " + part);
      }
      if (cnt != k) {
        err++;
      }
      cnt = 0;
      part = 0;
    }
    System.err.println("#Overlay done ");
    return g;
  }

  
  /* //OLDER VERSION DO NOT WORK GOOD!
  public static Graph wireKOutUnd2(Graph g, int k, Random r) {
    int deb = 10;
    if (deb > 0) {
      System.out.println("Nodes " + g.size() + " links " + k);
    }
    final int n = g.size();
    // solo 1 nodo
    if (n < 2) {
      return g;
    }
    // piu` archi dei vertici presenti
    if (n <= k) {
      k = n - 1;
    }
    int[][] matrix = new int[n][n];
    //init adjacency matrix
    for (int i = 0; i < n; ++i) {
      for (int j = 0; j < n; ++j) {
        matrix[i][j] = 0;
      }
    }
    if (deb > 0) {
      System.out.println("Adjacency matrix initilized and empty.");
    }
    //matrice usata per permutare gli indici dei nodi
    int nodes[] = new int[n];
    for (int i = 0; i < n; ++i) {
      nodes[i] = i;
    }
    //permutazione indici
    for (int i = 0; i < n; ++i) {
      int j = r.nextInt(n);
      int tmp = nodes[i];
      nodes[i] = nodes[j];
      nodes[j] = tmp;
    }
    if (deb > 0) {
      System.out.println("Permutation done.");
    }
    for (int i = 0; i < n; i++) {
      int links = 0;
      if (deb > 0) {
        System.out.print("\nNode #" + i + " >>> ");
      }
      //controllo il numero di links per nodo
      for (int c = 0; c < n; c++) {
        if (deb > 0) {
          System.out.print(matrix[i][c] + ", ");
        }
        if (matrix[i][c] == 1) {
          links++;
        }
      }
      if (deb > 0) {
        System.out.println(" : links " + links);
      }
      int j = n;
      // j prende il valore dell'ultimo nodo
      if (deb > 0) {
        System.out.println("Filling node " + i);
      }
      //creo archi finche` posso
      while (links < k && j > 0) {
        int id = r.nextInt(j);//estraggo un id nodo per il nodo  BETA
        int cnd_link = 0;//# link del nodo BETA
        for (int c = 0; c < n; c++) {//conto quanti links ha il nodo BETA
          if (matrix[nodes[id]][c] == 1) {
            cnd_link++;
          }
        }
        //Attualmente sto riempiendo la riga del nodo i detto ALFA
        while ((matrix[i][nodes[id]] != 0 || nodes[id] == i || cnd_link == k) && j > 1) {
          //se esiste gia` un link tra ALFA e BETA oppure se ALFA e BETA sono gli stessi nodi
          //oppure se il numero di link di BETA e`  pari al numero di archi massimo
          //oppure se non ci sono piu` nodi disponibili per collegarsi a ALFA
          if (deb > 0) {
            System.out.println("Need another node J:: " + j + ", nodes[" + id + "]=" + nodes[id] + "; Matrix [" + i + "][" + nodes[id] + "]=" + matrix[i][nodes[id]]);
          }
          if (deb > 0) {
            System.out.println("\tChange nodes[" + (j - 1) + "]=" + nodes[j - 1] + " with nodes[" + id + "]=" + nodes[id]);
          }
          int tmp = nodes[j - 1];
          nodes[j - 1] = nodes[id];
          nodes[id] = tmp;
          j--;
          id = r.nextInt(j);
          cnd_link = 0;
          for (int c = 0; c < n; c++) {
            if (matrix[nodes[id]][c] == 1) {
              cnd_link++;
            }
          }
        }
        if (j > 0) {
          if (nodes[id] != i) {
            matrix[i][nodes[id]] = 1;
            matrix[nodes[id]][i] = 1;
            links++;
            if (deb > 0) {
              System.out.println("J:" + j + ", nodes[" + id + "]=" + nodes[id] + "; Matrix [" + i + "][" + nodes[id] + "]=" + matrix[i][nodes[id]]);
            }
            if (deb > 0) {
              System.out.println("\tChange nodes[" + (j - 1) + "]=" + nodes[j - 1] + " with nodes[" + id + "]=" + nodes[id]);
            }
            int tmp = nodes[j - 1];
            nodes[j - 1] = nodes[id];
            nodes[id] = tmp;
          }
          j--;
        }
      }
    }
    int out_links_counter = 0;
    for (int i = 0; i < n; i++) {
      if (deb > 0) {
        System.out.println("Analyzing Node " + i);
      }
      out_links_counter = 0;
      for (int d = 0; d < n; d++) {
        if (deb > 0) {
          System.out.print(matrix[i][d] + ", ");
        }
        out_links_counter += matrix[i][d];
      }
      if (k - out_links_counter > 1 && (n - out_links_counter - 1) > 0) {
        if (deb > 0) {
          System.out.println("\n\t>>> Node " + i + " need help, it has " + out_links_counter + " links instead of " + k + " ... ");
        }
        nodes = new int[n - out_links_counter];
        int index = 0;
        for (int d = 0; d < n; d++) {
          if (matrix[i][d] == 0 && d != i) {
            nodes[index++] = d;
            if (deb > 0) {
              System.out.print(d + ", ");
            }
          }
        }
        int first = nodes.length;
        while ((k - out_links_counter) > 1 && first > 0) {
          int tg1 = r.nextInt(first);
          if (matrix[i][nodes[tg1]] == 0) {//filtro solo i link con cui  il nodo difettoso nn ha legami
            first--;
            int tmp = nodes[first];
            nodes[first] = nodes[tg1];
            nodes[tg1] = tmp;
            tg1 = nodes[first];
            int len = 0;
            int tg2[] = new int[n];
            if (deb > 0) {
              System.out.print("\tLink tagliabili da " + tg1 + " a :");
            }
            for (int d = 0; d < n; d++) {
              if (matrix[tg1][d] == 1 && matrix[i][d] == 0) {
                tg2[len] = d;
                len++;
                if (deb > 0) {
                  System.out.print(d + ", ");
                }
              }
            }
            int tg3 = r.nextInt(len);

            matrix[tg1][tg3] = matrix[tg3][tg1] = 0;
            matrix[i][tg1] = matrix[tg1][1] = 1;
            if (deb > 0) {
              System.out.println("\n\tTAGLIO link " + tg1 + " <=>" + tg3);
            }
            out_links_counter++;
            matrix[i][tg3] = matrix[tg3][1] = 1;
            out_links_counter++;
            if (deb > 0) {
              System.out.println("\tOutlink " + out_links_counter + " New row for " + i + ": ");
            }
            if (deb > 0) {
              for (int d = 0; d < n; d++) {
                if (deb > 0) {
                  System.out.print(matrix[i][d] + ", ");
                }
              }
            }
            if (deb > 0) {
              System.out.println();
            }
          }
          if (deb > 0) {
            System.out.println("K " + k + " Outlink " + out_links_counter + " First " + first);
          }
        }
      }
      if (deb > 0) {
        System.out.println();
      }
    }
    if (deb > 0) {
      for (int i = 0; i < n; i++) {
        out_links_counter = 0;
        for (int d = 0; d < n; d++) {
          System.out.print(matrix[i][d] + "|");
          out_links_counter += matrix[i][d];
        }
        if (k - out_links_counter > 1) {
          System.out.print(">>> Node " + i + " need help, it has " + out_links_counter + " links instead of " + k + " ... ");
        }
        System.out.print("\n");
      }
    }
    int part = 0;
    for (int i = 0; i < n; i++) {
      if (deb > 0) {
        System.out.print("Node " + i + " :[ ");
      }
      for (int j = 0; j < n; j++) {
        if (matrix[i][j] == 1) {
          if (i == j) {
            if (deb > 0) {
              System.out.println("??Errore!!");
            }
            part++;
          } else {
            if (deb > 0) {
              System.out.print(j + ",");
            }
            g.setEdge(i, j);
            g.setEdge(j, i);
          }
        }
      }
      if (deb > 0) {
        System.out.println("] " + part);
      }
      part = 0;
    }

//         ArrayList rich = new ArrayList();
//        ArrayList poor = new ArrayList();
//        for (int i = 0; i < n; i++) {
//            System.out.print("<" + i + "> ");
//            out_links_counter = 0;
//            for (int d = 0; d < n; d++) {
//                System.out.print(matrix[i][d] + "|");
//                out_links_counter += matrix[i][d];
//            }
//            if (out_links_counter < k) {
//                poor.add(i);
//                System.out.print(">>> Node " + i + " it has LESS " + out_links_counter + " links instead of " + k + " ... ");
//            } else if (out_links_counter > k) {
//                System.out.print(">>> Node " + i + " it has MORE " + out_links_counter + " links instead of " + k + " ... ");
//                rich.add(i);
//            } else {
//                out_links_counter = 0;
//            }
//            System.out.print("\n");
//        }
//
//        for(int i = 0 ; i <= poor.size(); i++){
//            int apoor = (Integer)poor.remove(0);
//            System.out.println("Poor "+apoor);
//            for(int j= 0 ; j<= rich.size(); j++){
//                int arich=(Integer)rich.remove(0);
//                System.out.println("Rich "+ arich);
//                int peerid =0;//PEER IN PIU`
//                System.out.println("finding last peer in RR "+arich);
//                int last = -1;
//                for(int z = 0; z < matrix[arich].length;z++){
//                    if(matrix[arich][z]>0 && z!=apoor)
//                        last = z;
//
//                }
//                System.out.println("["+arich+","+last+"]="+matrix[arich][last]);
//                        peerid = last;
//                        matrix[arich][last] = 0;
//                }
//                System.out.println("finding last hole in poor");
//                for(int z = 0; z < matrix[apoor].length && matrix[apoor][z]>0;z++){
//                    if(matrix[apoor][z]==0){
//                        matrix[apoor][z]=peerid;
//                        System.out.println("Last hole is "+z);
//                    }
//                }
//                System.out.println("finding " + arich+" in "+peerid);
//                for(int z = 0; z < matrix[peerid].length && matrix[peerid][z]>0;z++){
//                    if(matrix[peerid][z]==arich){
//                        System.out.println("is in pos "+z);
//                        matrix[apoor][z]=apoor;
//
//                    }
//                }
//
//            }
//
//        }
//
//        while(!rich.isEmpty()&&rich.size()%2==0){
//                int arich=(Integer)rich.remove(0);//14
//                System.out.println("Rich "+ arich+" " +rich.size());
//                int brich=(Integer)rich.remove(0);//16
//                int last_arich = matrix[arich][k];//45
//                int last_brich = matrix[brich][k];//47
//                matrix[arich][k]=-1;
//                matrix[brich][k]=-1;
//                for(int i = 0; i < matrix[last_arich].length; i++)
//                    if(matrix[last_arich][i]== arich)
//                        matrix[last_arich][i]= last_brich;
//                for(int i = 0; i < matrix[last_brich].length; i++)
//                    if(matrix[last_brich][i]== brich)
//                        matrix[last_brich][i]= last_arich;
//            }
//
//        for (int i = 0; i < n; i++) {
//            System.out.print("<" + i + "> ");
//            out_links_counter = 0;
//            for (int d = 0; d < n; d++) {
//                System.out.print(matrix[i][d] + "|");
//                out_links_counter += matrix[i][d];
//            }
//            if (out_links_counter < k) {
//                poor.add(i);
//                System.out.print(">>> Node " + i + " it has LESS " + out_links_counter + " links instead of " + k + " ... ");
//            } else if (out_links_counter > k) {
//                System.out.print(">>> Node " + i + " it has MORE " + out_links_counter + " links instead of " + k + " ... ");
//                rich.add(i);
//            } else {
//                out_links_counter = 0;
//            }
//            System.out.print("\n");
//        }
//
//        for(int i = 0 ; i < poor.size(); i++){
//            int apoor = (Integer)poor.remove(0);
//            System.out.println("Poor "+ apoor);
//            for(int j= 0 ; j<= rich.size(); j++){
//                int arich=(Integer)rich.remove(0);
//                System.out.println("Rich "+ arich);
//            }
//
//        }

    System.err.println("#Overlay done");
    return g;
  }*/

//----------------------------------------------------------------
/*
public static void main(String[] pars) {
        int n = Integer.parseInt(pars[0]);
        int k = Integer.parseInt(pars[1]);
        Graph g = new BitMatrixGraph(n);
        
        wireWS(g,20,.1,new Random());
        GraphIO.writeChaco(new UndirectedGraph(g),System.out);
        
        wireScaleFreeBA(g,3,new Random());
        wireKOut(g,k,new Random());
        wireRegRootedTree(g,k);
        wireHypercube(g);
        wireKOutUnd(g, k, new ExtendedRandom(k));
        GraphIO.writeNeighborList(g,System.out);
}
*/

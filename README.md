# Info
A comparison study between different FFT algorithms implemented in Java as part of the bachelor's degree. Implemented algorithms: Furier transform by definition, radix-2 (DIT) recursive, radix-2 (DIT) iterative, radix-2 (DIF) recursive, radix-4 (DIT) recursive, radix-4 (DIF) recursive, radix-4 (DIT) iterative, split radix (DIT), split radix (DIF), Bluestein's algorithm (which uses radix-2 DIT for convolution).

Algorithms were compared with help of the [ALGator](https://github.com/ALGatorDevel/Algator) system.

Each implementation which we mentioned above can be found in: PROJ-FurierTransform/algs/ALG-\<algorithm_name\>

* **Author**: Žiga Zorman
* **Mentor**: Tomaž Dobravec, Ph.D.

## Dependencies
* [Apache Commons Math 3.6.1](http://commons.apache.org/proper/commons-math/download_math.cgi)


## Downloads
* [Test results](https://drive.google.com/file/d/0B3wxW4hL0-evOXNEc0huaklmc1U/view?usp=sharing)

## References

* Henri J. Nussbaumer, "Fast Fourier Transform and Convolution Algorithms: Second Corrected and Updated Edition",2. ed., *Springer Series in Information Science*, vol. 2, page 81-94, Berlin [etc.]: Springer, 1982.

* E. Chu, A. George "Inside the FFT Black Box: Serial and Parallel Fast Fourier Transform Algorithms", *Computational Mathematics Series*, Boca Raton: CRC Press LLC, 2000.

* John G. Proakis, Dimitris K. Manolakis "Digital Signal Processing: Principles, Algorithms, and Applications", 4. ed., New Jersey: Prentice Hall, 7(4), 2006.

* P. Duhamel, Henk D. L. Hollmann. "Split radix FFT algorithm". [Online](https://www.researchgate.net/publication/3399594_Split_radix)

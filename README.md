<div id="top"></div>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->





<!-- PROJECT LOGO -->
<br />
<div align="center">
  <h3 align="center">Bot Override - Capcay Kuah</h3>

  <p align="center">
    Sebuah Project Bot Balapan Override dengan bahan dasar Java
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->

## About The Project


Bot override ini dibuat menggunakan Bahasa Java. Bot ini memanfaatkan template yang diberikan oleh entelect. Tujuan dari Bot ini adalah mencapai garis finish tercepat. Pada implementasinya, bot ini menggunakan algoritma greedy Bagian utama dari bot ini adalah file bot.java, yang merupakan file yang dieksekusi tiap turn oleh program. untuk informasi lengkap mengenai algoritma, bisa di cek di folder doc maupun di folder `src` dengan nama file `Bot.java`\
Untuk mengetahui lebih lengkap tentang game ini, cek [di sini](https://github.com/EntelectChallenge/2020-Overdrive)

<p align="right">(<a href="#top">back to top</a>)</p>


### Greedy Algorithm
Program memilih langkah-langkah yang menghasilkan jalan lajur tercepat dan terefektif untuk mencapai garis finish. Melalui lajur yang memiliki hambatan terkecil atau power up terbesar di antara jalur lainnya. Serta, langkah untuk mengeluarkan power up dan FIX seoptimal dan efektif mungkin, bergantung pada state dari permainan.
Algoritma akan me-asses beban atau nilai dari tiap lajur yang akan dilalui, dengan pivot lajur tempat kendaraan berada saat itu. Dalam tiap asses yang dilakukan, hambatan akan dinilai sebagai beban, dan power up akan dinilai sebagai profit. Algoritma memilih langkah yang memiliki beban terkecil. Perlu diperhatikan, tiap jenis hambatan nilainya berbeda. Tergantung efek keterlambatan yang ditimbulkan akibat menabrak tiap hambatan. 
Selain hambatan dan power up, hal yang perlu diperhatikan adalah kecepatan. Tiap belokan yang dilakukan akan mengurangi kecepatan, sedangkan jika tetap lurus, dan kecepatan belum maksimal, bot akan mengakselerasi mobil. <br> <br>
Maka, alternatif algoritma greedy ini dalam beberapa tahap:
1. Memeriksa apakah lurus merupakan jalur optimal (dalam 2 langkah). Jika ya, maka akan dikembalikan ACCELERATE.
2. Memeriksa apakah belok merupakan jalur optimal (dalam 2 langkah). Jika ya, maka akan dikembalikan TURN.
3. Jika 1 dan 2 tidak terpenuhi, maka pilih jalur dengan nilai kolisi terkecil.
<br> 
Ketiga tahap tersebut dilakukan 2 kali iterasi, yaitu dengan current reachable (jarak yang dapat dicapai saat ini), lalu diperiksa kembali dari current reachable.

### Built With

Bot ini dibuat dengan teknologi berikut.
* [Java](https://www.oracle.com/java/technologies/downloads/)

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- GETTING STARTED -->

## Getting Started

Berikut adalah sedikit instruksi mengenai penggunaan bot.

### Prerequisites

This is an example of how to list things you need to use the software and how to install them.
* [Java](https://www.oracle.com/java/technologies/downloads/#java8)
* [IntelIiJ IDEA (to compile)](https://www.jetbrains.com/idea/)
* [NodeJS](https://nodejs.org/en/download/)

### Installation

_Untuk menjalankan bot ini kita akan bagi menjadi dua os, linux dan Windows._
Untuk Linux,
1. Buka terminal pada direktori utama bot.
2. Jalankan pada terminal
   ```
   make run
   ```
   
Untuk Windows
1. Jalankan program `run.bat` dengan mengekliknya
 
Bagi anda yang ingin melihat visualisasi lebih baik, anda dapat menggunakan visualizer.

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- CONTACT -->

## Contact
<!-- sngaja diacak -->

Ng Kyle		 - 13520040 [@Nk-Kyle](https://github.com/Nk-Kyle)\
Muhammad Risqi Firdaus	 - 13520043 [@mrfirdauss-20](https://github.com/mrfirdauss-20)\
Cristopher Jeffrey	 - 13520055 [@christojeffrey](https://github.com/christojeffrey)


<p align="right">(<a href="#top">back to top</a>)</p>




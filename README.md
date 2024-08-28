# MovieApp 🎬

**MovieApp**, Android platformunda Kotlin dili kullanılarak geliştirilmiş bir mobil uygulamadır. Bu uygulama, kullanıcılara popüler, en çok oy alan, vizyondaki ve yakında çıkacak filmler hakkında bilgi sunar. Kullanıcılar film arayabilir, film detaylarını görüntüleyebilir ve favori listelerine ekleyebilirler.

## Özellikler 📱

- **Film Arama**: Kullanıcılar anahtar kelimelerle film arayabilir.
- **Popüler Filmler**: Güncel popüler filmleri listeler.
- **En Çok Oy Alan Filmler**: En yüksek oylamaya sahip filmleri gösterir.
- **Vizyondaki Filmler**: Halen vizyonda olan filmleri listeler.
- **Yakında Çıkacak Filmler**: Yakında çıkacak olan filmleri gösterir.
- **Film Detayları**: Her film için ayrıntılı bilgi ve fragman izleme seçeneği sunar.
- **Favorilere Ekleme**: Kullanıcılar favori filmlerini kaydedebilir.

## Ekran Görüntüleri 📸

![Ana Sayfa](screenshots/home_screen.png)
![Film Detayı](screenshots/detail_screen.png)
![Favoriler](screenshots/favorites_screen.png)

## Kurulum ve Kullanım 🛠️

### Gereksinimler

- Android Studio Arctic Fox veya daha yeni bir sürüm
- Minimum API seviyyesi: 21 (Android 5.0 Lollipop)

### Kurulum

1. Bu depoyu klonlayın:
    ```bash
    git clone https://github.com/kullaniciadi/MovieApp.git
    ```
2. Android Studio'yu açın ve proje klasörünü içe aktarın.
3. Gerekli bağımlılıkların yüklü olduğundan emin olmak için projeyi senkronize edin.
4. Bir Android cihaz veya emülatör bağlayın ve uygulamayı çalıştırın.

## Kullanılan Teknolojiler ve Kütüphaneler 🛠️

- **Kotlin**: Uygulamanın yazılım dili.
- **Jetpack Compose**: UI tasarımı ve yönetimi için modern bir toolkit.
- **Dagger-Hilt**: Dependency Injection (Bağımlılık Enjeksiyonu) için.
- **Retrofit**: REST API çağrıları için.
- **Room**: Yerel veritabanı yönetimi için.
- **Coil**: Görüntü yüklemek için.
- **Navigation Component**: Uygulama içi gezinme için.

## API Anahtarı Ekleme 🔑

Uygulamanın çalışabilmesi için bir film API anahtarına ihtiyacınız var. Lütfen şu adımları izleyin:

1. [The Movie Database (TMDb)](https://www.themoviedb.org/) web sitesinden bir API anahtarı alın.
2. `local.properties` dosyasına `API_KEY=YOUR_API_KEY` şeklinde ekleyin.

## Katkıda Bulunma 🤝

1. Bu depoyu fork edin.
2. Yeni bir özellik eklemek veya hata düzeltmek için bir dal oluşturun (`git checkout -b feature-adi`).
3. Değişikliklerinizi yapın ve commit edin (`git commit -m 'Açıklama'`).
4. Dalınızı bu depoya push edin (`git push origin feature-adi`).
5. Bir Pull Request gönderin.

## Lisans 📜

Bu proje MIT Lisansı ile lisanslanmıştır. Daha fazla bilgi için `LICENSE` dosyasına bakın.

## İletişim 📧

- **Geliştirici**: [Adınız](https://github.com/kullaniciadi)
- **E-posta**: email@example.com

Her türlü geri bildiriminizi ve önerinizi duymaktan memnuniyet duyarız! 🎉

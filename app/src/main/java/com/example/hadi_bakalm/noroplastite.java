package com.example.hadi_bakalm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hadi_bakalm.data.AppDatabase;
import com.example.hadi_bakalm.model.ConceptItem_kavram;

import java.util.List;

public class noroplastite extends AppCompatActivity {

    private ImageView btnBack, btnMoreMenu;
    private FrameLayout btnDialogues, btnImportance;
    private View contentDialogues, contentImportance;

    // Kaydet Butonu ve Veri Tabanı Bileşenleri
    private LinearLayout btnKaydet;
    private TextView txtKaydet;
    private AppDatabase db;
    private ConceptItem_kavram currentConcept;

    // Panoya Kopyalama Yardımcı Metodu
    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, label + " panoya kopyalandı", Toast.LENGTH_SHORT).show();
        }
    }

    // Metin Paylaşma Yardımcı Metodu
    private void shareText(String title, String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Şununla paylaş:"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noroplastite);

        // --- 1. Veri Tabanı Bağlantısı ---
        db = AppDatabase.getInstance(this);

        // --- 2. Görünüm Bağlantıları (XML ID'leri) ---
        btnBack = findViewById(R.id.btnBack);
        btnMoreMenu = findViewById(R.id.btnMenu);
        btnKaydet = findViewById(R.id.btnSave);
        txtKaydet = findViewById(R.id.kaydet_butonu);

        TextView txtConceptTitle = findViewById(R.id.txtConceptTitle);
        TextView txtConceptDescription = findViewById(R.id.txtConceptDescription);
        TextView txtPersonalNote = findViewById(R.id.txtPersonalNote);
        TextView txtDialoguesContent = findViewById(R.id.txtDialoguesContent);
        TextView txtImportanceContent = findViewById(R.id.txtImportanceContent);

        TextView btnCopy1 = findViewById(R.id.btnCopy1);
        TextView btnShare1 = findViewById(R.id.btnShare1);
        TextView btnCopy2 = findViewById(R.id.btnCopy2);
        TextView btnShare2 = findViewById(R.id.btnShare2);

        btnDialogues = findViewById(R.id.btnDialogues);
        btnImportance = findViewById(R.id.btnImportance);
        contentDialogues = findViewById(R.id.contentDialogues);
        contentImportance = findViewById(R.id.contentImportance);

        // --- 3. Tıklanan Kavrama Göre Verileri Dinamik Doldur ---
        String gelenKavram = getIntent().getStringExtra("KAVRAM_ADI");
        setupConceptData(gelenKavram, txtConceptTitle, txtConceptDescription, txtPersonalNote, txtDialoguesContent, txtImportanceContent);

        // --- 4. Kaydet Butonu Tıklama Olayı ---
        if (btnKaydet != null) {
            btnKaydet.setOnClickListener(v -> {
                if (currentConcept != null) {
                    boolean newSavedState = !currentConcept.isSaved();
                    currentConcept.setSaved(newSavedState);

                    new Thread(() -> {
                        db.conceptDao_kavram().update(currentConcept);
                    }).start();

                    updateSaveButtonUI();

                    if (newSavedState) {
                        Toast.makeText(this, "Kayıtlı kavramlara eklendi", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Kayıtlı kavramlardan çıkarıldı", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // --- 5. 3 Nokta Menüsü Tıklama Olayı ---
        if (btnMoreMenu != null) {
            btnMoreMenu.setOnClickListener(this::showPopupMenu);
        }

        // --- 6. Kopyala / Paylaş Tıklama Olayları ---
        if (btnCopy1 != null && txtConceptDescription != null) {
            btnCopy1.setOnClickListener(v -> copyToClipboard("Kavram Açıklaması", txtConceptDescription.getText().toString()));
        }

        if (btnShare1 != null && txtConceptDescription != null) {
            btnShare1.setOnClickListener(v -> shareText("Kavram Açıklaması", txtConceptDescription.getText().toString()));
        }

        if (btnCopy2 != null && txtPersonalNote != null) {
            btnCopy2.setOnClickListener(v -> copyToClipboard("Kişisel Not", txtPersonalNote.getText().toString()));
        }

        if (btnShare2 != null && txtPersonalNote != null) {
            btnShare2.setOnClickListener(v -> shareText("Kişisel Not", txtPersonalNote.getText().toString()));
        }

        // --- 7. Geri Butonu ve Açılır/Kapanır Panel Mantıkları ---
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnDialogues != null && contentDialogues != null) {
            btnDialogues.setOnClickListener(v -> {
                contentDialogues.setVisibility(contentDialogues.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }

        if (btnImportance != null && contentImportance != null) {
            btnImportance.setOnClickListener(v -> {
                contentImportance.setVisibility(contentImportance.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        }
    }

    private void setupConceptData(String kavramAdi, TextView txtTitle, TextView txtDesc, TextView txtNote, TextView txtDialogues, TextView txtImportance) {
        if (kavramAdi == null) kavramAdi = "Nöroplastisite";

        String title = "", desc = "", note = "", dialogues = "", importance = "";

        if (kavramAdi.equalsIgnoreCase("amigdala")) {
            title = "Amigdala";
            desc = "Amigdala Beynin en eski bölgelerinden biridir, bir nevi eski kurt işte. Temelde tehdit ve ödül algısı ile çalışır ve saniyeler içinde tepki verir. Mantıktan ziyade çok düz düşünür. Görevleri ve mantığı şunlardır:\n\n" +
                    "Düz ve Hızlı Algı: Öyle aman aman detaylı, analitik düşünme mekanizması yoktur. Sürekli olarak \"Bu bir tehdit mi, yoksa ödül mü?\" kıvamında çalışır.\n\n" +
                    "Hayatta Kalma Refleksi: Tüm amaç hızlıca tepki vermektir. Eğer önünde bir ödül varsa o ödüle yaklaşıp ulaşmanı, bir tehdit varsa da oradan saniyeler içinde uzaklaşmanı sağlar.\n\n" +
                    "İlkel Dürtülerin Kaynağı: Günümüzde \"ilkel dürtü\" dediğimiz şeylerin atası tam olarak budur. Bu yüzden o ilkel dürtüleri o an canlı canlı yaşayan insanlarda çok güçlü bir canlılık ve harika bir his (adrenalin/coşku) oluşur.\n\n" +
                    "Sistemdeki Yeri: Buna artık \"alt beyin\" mi dersiniz ya da \"ilkel beyin\" mi dersiniz o size kalmış; ama işin özü ve temelindeki olay tamamen budur.\n\n\n" +
                    "Amigdala - Mikro-Örnekler\n" +
                    "Örnek 1: Yerde duran kıvrılmış siyah bir ip parçasını anlık olarak yılan sanıp irkilerek geriye doğru fırlamak.\n\n" +
                    "Örnek 2: Topluluk önünde konuşma yaparken, ortada fiziksel bir tehlike olmamasına rağmen kalp atışlarının hızlanması ve avuç içlerinin terlemesi.\n\n" +
                    "Örnek 3: Trafikte bir aracın aniden önüne kırmasıyla, hiç düşünmeden kornaya basıp saniyeler sonra olayın şokunu atlatmaya çalışmak.\n\n" +
                    "Örnek 4: Korku filmi izlerken ekrana aniden fırlayan bir görüntüyle birlikte, güvende olduğunu bilsen bile refleks olarak yerinden sıçramak.\n\n" +
                    "Örnek 5: İş yerinde yöneticiden gelen \"Odama gel, konuşalım\" mesajını okur okumaz, ortada hiçbir sebep yokken anında panikleyip en kötü senaryoları kurmaya başlamak.\n\n" +
                    "Örnek 6: Arkadaş ortamında yapılan sığ bir eleştiriye, mantıklı analiz etmek yerine anında amansız bir savunmaya geçip ses tonunu yükseltmek.\n" +
                    "Başka örnekler de var. Bunlarla sınırlı düşünmeyin.";

            note = "Geldik kişisel düşüncelerime şimdi. Burada işler biraz değişiyor işte. Şimdi şu amigdala var ya hani; işte o kısım hem en büyük gücünüz hem de en büyük zafiyetiniz. Hani o pazarlamacılar, reklamcılar falan var ya; sizin o mantık ile düşünen kısmınızı değil, amigdalanızı hedef alıyorlar. Neden koltuk reklamlarında alakasız güzel kadınlar var sanıyorsunuz?\n" +
                    "Tabii ki bunları zaten bilip 'Bunları zaten biliyorum. Neden tekrar anlatıyorsun ki?' diyenler olacak ama bilmeyenler için anlatıyorum bunu. Amigdalanızı hedefliyorlar; onu koruyun dış saldırılardan. Bu öyle bir nimettir ki doğru yaklaşımla size kaldıraç olur amigdalanın açıklar ya da tam tersi başkalarının kolayca ulaştığı oyuncak olur. Tabii ki hissedin, edin, hayatı yaşayın ama o biricik kalenize girmelerine izin vermeyin o nöro-pazarlamacıların. Ya da o amigdalanın açıklarını kullanıp o mantıklı düşünme kısmınıza perde çekmelerine (örneğin toplu nefret kusma birine karşı [bu herhangi biri olabilir] durumu buna güzel bir örnektir bence) izin vermeyin.";

            dialogues = "Örnek diyalog 1:\n6 kişilik bir erkek arkadaş grubu, birinin evinde toplanıp maç izlemektedir. Tam 90+da gol atılacakken top direğe çarpar. Herkes ayağa fırlar, bağırıp çağırır. O ilk şok dalgası geçtikten sonraki dakikalarda, gruptakiler koltuklara çöküp söylenmeye ve dert yanmaya devam ederken, ev sahibi ile arkadaşı arasında sessiz bir konuşma geçer.\nEv Sahibi: Dürüst ol. Maç bittiğinden beri bizimle birlikte dert yanıp aynı tepkileri veriyorsun ama sırf biz öyle yaptığımız için öyle davrandın, değil mi?\nArkadaşı: Yo, ne alaka?\nEv Sahibi: Ben seni tanıyorum. Futbolla o kadar ilgili değilsin. Topluca biz o moda girdiğimiz için amigdalan otomatik olarak gruba uyum sağlama ihtiyacı hissetti. Ama bunda sorun yok, arkadaşız sonuçta. Sadece fark et istedim.\nArkadaşı: (İçinden der ki) Doğru valla, ortamın gazına gelip ben de başladım söylenmeye. \nSonra gece her şey yolunda bir şekilde devam eder.\n\n" +
                    "Örnek diyalog 2:\nİki yakın arkadaş birlikte film izliyordur (mesela Titanic olsun). Tam da o meşhur, internette 'Titanic flying scene' diye aratırsanız bulabileceğiniz ikonik gemi burnunda kolları açma sahnesindedirler. Arkadaşlardan biri der ki:\n— Kanka aklıma harika bir fikir geldi ama önce film bitsin.\nFilm biter. O arkadaş, jenerik akar akmaz hızlıca laptopuna sarılır ve hemen bir video düzenler. Arkadaşına gösterir sonrasında:\n— Kanka şuna baksana bir.\nArkadaşı ekrana bakınca ne görsün; bizimki o aşırı romantik ve duygusal sahnenin arkasına palyaço müziği koymuştur ve video aşırı absürt, garip bir hal almıştır. Tabii doğal bir şaşkınlıkla der ki:\n— Bu ne la?\nÖbürü gülerek açıklar:\n— Bizim o devasa romantik sahnenin farklı bir müzikle nasıl 180 derece değişeceğine dair canlı bir kanıt işte! Müziğin gücü işte. Nasıl da algılarımıza hükmediyor görüyorsun.\nArkadaşı kafasını sallar:\n— Teşekkürler valla. Sayende artık bu sahneyi her gördüğümde arkada çalan o palyaço müzikli videon aklıma gelecek.";

            importance = "Bu amigdalanın pratik hayattaki önemi: Şimdi bazılarınız soracak: 'Bu kavramı öğrenmemin faydalı olacağını söylüyorsun da, neden faydalı olacak ki? Neden zaman ve enerji harcayıp bu kavramı öğreneyim?' dercesine. Onu da madde madde açıklayayım (her ne kadar hepsine değinemeyecek olsam da):\n\n" +
                    "Manipülasyon Kalkanı: Belli bir farkındalık ve çabanın sonucunda; başkaları sizin duygularınızı, korkularınızı ya da o 'bir topluluğun içinde bulunma, dışlanmama' gibi ilkel dürtülerinizi kullanarak sizi kolayca yönlendiremez.\n\n" +
                    "Enerji Dönüşümü: Öfkenizi ya da anlık yükselen duygularınızı havaya, boşluğa veya etrafınıza saçıp tüketmek yerine; ya cidden ulaşması gereken mantıklı bir yere yönlendirirsiniz ya da kendiniz için faydalı bir enerji ve istek üretim mekanizmasına dönüştürme fırsatı yakalarsınız.\n\n" +
                    "Kriz Anında Direksiyon Hakimiyeti: Örneğin bir sorun yaşadığınızda; o anki öfke, kırgınlık veya kıskançlık gibi bir duyguya kapıldığınız an bu mekanizmanın farkında olursunuz. Kendinize, 'Eğer kendimi şu an bu hisse kaptırırsam geçici bir rahatlama yaşayacağım ama sorunum çözülmeyecek' diyebilir ve tekrar mantıklı düşünebilmeye başlarsınız. (Bunu %100 yapabilirsiniz diyemem ama en azından o an ne yaşadığınızın farkında olursunuz.)\ngibi gibi";

        } else if (kavramAdi.equalsIgnoreCase("pfc")) {
            title = "PFC(prefrontal korteks)";
            desc = "PFC(prefrontal korteks): Bizim mantık ve analiz yapan beyin kısmımızdır. Evrimsel süreçte insanın geçirdiği değişimlerle birlikte bugünkü muazzam gücüne yeni yeni ulaştı burası; sürüye bir nevi yeni katıldı yani. Denecek çok bir şey yok aslında bunun için. Değinilmesi gereken şunlar var:\n\n" +
                    "Çok Fazla Enerji Tüketir: İlkel beyne ve temel hayatta kalma fonksiyonlarına kıyasla korkunç bir enerji harcar. O yüzden uzun süre yüksek performansla çalıştırmak beyin için çok maliyetlidir.\n\n" +
                    "Güç Dengesi ve Ortaklık: Limbik/ilkel sistem (amigdala yani) onun yanında çok daha eski ve evrimsel olarak daha köklü olduğu için, bazı kriz anlarında PFC'ye giden enerjiyi kolayca kesip kontrolü ele alabilir. Ama normal şartlarda ve çoğu durumda ortak çalışırlar. Biri 0 olup diğerinin 1 olması gibi katı bir kural yok.\n\n" +
                    "İşin özü; mantık, planlama ve analiz kısımlarını tamamen burası üstleniyor işte.\n\n\n" +
                    "PFC - Mikro-Örnekler\n" +
                    "Örnek 1: Alışveriş sitesinde çok beğendiğin pahalı bir kıyafeti sepete eklemişken, aylık bütçeni ve kredi kartı limitini düşünerek sekmeyi sakince kapatmak.\n\n" +
                    "Örnek 2: Sabah sıcacık yataktan çıkmak istemeyip alarmı erteleme dürtüsü gelse de, işe veya derse geç kalmamanın uzun vadedeki sorumluluğuyla hemen ayağa fırlamak.\n\n" +
                    "Örnek 3: Bir projeye çalışırken telefonuna sürekli gelen sosyal medya bildirimlerini görmezden gelip, cihazı sessize alarak önündeki işe odaklanmaya devam etmek.\n\n" +
                    "Örnek 4: Karşındaki insanın sana kaba ve haksız bir ithamda bulunmasıyla içinden ona bağırmak gelse de, derin bir nefes alıp durumu profesyonelce ve sakince çözmeye çalışmak.\n\n" +
                    "Örnek 5: Hafta sonu arkadaşların eğlenmeye çağırırken, pazartesi günü gireceğin önemli sınavı düşünerek onlara \"Hayır\" demek ve evde kalıp ders çalışmak.\n\n" +
                    "Örnek 6: Akşam saatinde canın aşırı derecede şekerli ve yağlı bir tatlı çekmesine rağmen, sağlıklı beslenme hedeflerini hatırlayıp onun yerine bir bardak su içerek mutfaktan uzaklaşmak.\n" +
                    "Başka örnekler de var. Bunlarla sınırlı düşünmeyin.";

            note = "Kişisel not: Evet, amigdaladan bahsetmiştik ve PFC de onun bir nevi zıt kardeşidir benim için. Burada denecek çok bir şey yok ama bir-iki şey diyebilirim.\n" +
                    "Öncelikle şu klasik klişe olan 'disiplin' ya da 'sıkı çalışma' şeyine bir netlik getirmemiz gerekiyor. Bazılarınız çıkıp, 'Ya disiplin işte, nesi eksik ki?' diyebilir. Şimdi, ben 'disiplin' kavramına karşı değilim; ben burada körlemesine biyolojik gerçekleri yok sayarak bunu yapmaktan bahsediyorum. 'Ama mola saati' falan deyip araya girmeyin şimdi eğer giren varsa, benim dediğim şey o değil.\n" +
                    "PFC'nin burada tamamen öne çıkarılıp ilkel dürtülerin ve amigdalanın yok sayılması sinir ediyor beni. Evet, tabii ki de tamamen PFC kaynaklı bir şekilde saf çalışma dediğimiz anlar da olacak belli ölçüde; ama bu iş 'Sen çok iradelisin, kafana koyarsan yaparsın' ile yürüyemez uzun vadede bence. O amigdalanın lojistik desteği (hormonlar falan) olmadan yürümez bu. Bir nevi iş birliği bildiğiniz işte.Bu yüzden biyolojik okuryazarlık benim için çok önemlidir. Bence doğru olan şey bu: Akıllı olup iki taraf (PFC ve amigdala) arasında bir uzlaşı sağlamak en doğrusudur bence. Ama bu da çok kişisel ve öznel bir şey işte. Size buradan 'Şunları şunları yapın' diyemem. En fazla kapıyı gösterebilirim yani.Durun... Aklıma bir şey daha geldi, bahsetmeden rahat duramayacağım. Bu PFC'nin şu yönü de kişisel olarak beni bir tık sinir ediyor: amigdala dediğimiz yer zaten en baştan kararı veriyor; PFC de adeta bir sözcü gibi onun bahanesini uyduruyor.\n" +
                    "Örnek mi istiyorsunuz? Vereyim: 'Senin için çok endişelendim, o yüzden telefondan açmamana rağmen 4 kez üst üste aradım.' Kulağa masum geliyor ve belki de cidden öyledir; ama madalyonun diğer yüzüne davet ediyorum sizi. Bu kişi o kadar korkmuş ki, kendi içindeki korkuyu tutamayıp 'Ya çok korktum ve bunu sana yansıttım' diyemediği için (onun da sebebi bilişsel çelişki ve bilişsel cimrilik yasası ama konumuz o değil şu an) 'Senin için çok endişelendim' bahanesine bağlanıyor.\n" +
                    "Evet biliyorum, bazılarınız 'Bu çok gaddarca bir düşünce. Ne güzel düşünceli bir insan işte, nankörlük bu' diyor olabilir. Ama burada, o benim tabirimle 'kullanıcı arayüzünü' kullanmıyoruz böyle analizlerde. Biyolojik gerçeğe bakıyoruz burada ve biyolojinin bir duygusu ya da 'nankörlük' gibi olguları yoktur. Kısacası PFC benim için, her durumda olmasa da bir açıdan gerçek düşünceleri örtücü bir suç ortağıdır.\n" +
                    "En nihayetinde önerim bu yani: Her durumda bunu yapmayın tabii ki ama yer yer o cümlenin altındaki kök biyolojik nedeni anlamaya çalışın. Çünkü cümle, her durumda olmasa da gerçeği yansıtmaz.";

            dialogues = "örnek diyaloglar:\nörnek diyalog 1:(sonra yazılacak)";

            importance = "pratik hayattaki önemi:bu pfc neden önemlidir peki pratik hayatta?şahsen benim için.günümüz dünyasında pfc çok kutsanıyor gibi geliyor bana.daha net konuşmak gerekirse sanki biyolojik mekanizmalarımız,ilkel dürtüler( ilkel dürtüler sadece cinsellikten ibaret değildir!) gibi şeyler yok sayulıyor sanki.mesela \"disiplin\" kavramını duymuşsunuzdur.ya tamam disiplin falanda bu disiplin tam olarak nedir?eğer burada bahsettiğimiz şey o bence hastalıklı olan \"mantıklı davran,düzenli ol,sıkı çalış ve asla durma\" mantalitesi ise ben burayı desteklemiyorum.ben biyoloji ile uyumlu olan bir \"disiplin\" anlayışını destekliyorum.mesela sadece bu pfc ile mantıkta mantık şeyine karşıyım ben.biyolojik olarak yer yer bilinçli olarak kişide eğer bunu yapabiliyorsa o çiğ ilkel dürtülerin desteğini alıp o mantık kısmını desteklenirken yer yer de tamamen pfc ile bu çalışmalar yapılmalıdır.biyolojik varlıklarız biz.kısacası mantık ve analiz için önemli işte.";

        } else if (kavramAdi.equalsIgnoreCase("Dopamin ve Dopamin Bazal Seviyesi")) {
            title = "Dopamin ve Dopamin Bazal Seviyesi";
            desc = "Tanım: Öncelikle dopamin, sanıldığının aksine bir 'haz hormonu' değildir. \n\n" +
                    "Dopamin, o hedefe ulaşırken salgılanan teşvik hormonudur. O hedefe ulaşmak, harekete geçmek için bu hormon şarttır; aksi takdirde en basit eylem olan yataktan kalkmayı bile başaramazdınız. \n\n" +
                    "*   Dopamin Bazal Seviyesi (Baseline): Sizin sisteminizin genel arka plan enerji seviyesidir. Bir nevi tetiklenme kapasiteniz ve dopamin reseptörlerinizin algılama eşiğidir. \n\n" +
                    "Basitçe, sizi hayatta tutan ve yürüten o ana teşvik motorudur. Bitti.\n\n\n" +
                    "Mikro-Örnekler\n" +
                    "Örnek 1: Bilgisayar başında saatlerce oyun oynadıktan sonra gelen aşırı doymuşluk hissiyle birlikte, ertesi gün en sevdiğin aktiviteleri yaparken bile canının hiçbir şey istememesi.\n" +
                    "Örnek 2: Zorlu bir sınav haftasını başarıyla geride bırakıp yüksek notlar almanın ardından gelen o büyük rahatlama hissiyle, sonraki birkaç gün boyunca ders kitaplarının yüzünü bile açacak enerjiyi kendinde bulamamak.\n" +
                    "Örnek 3: Sosyal medyada saatlerce aşağı kaydırarak hızlı ve emeksiz videolar izledikten sonra telefon ekranını kapattığın an, odadaki sessizlikte hissettiğin o derin anlamsızlık ve motivasyon çöküşü.\n" +
                    "Örnek 4: Günlerdir heyecanla beklediğin ve internetten sipariş ettiğin kargonun kapıya ulaştığı o saniyede yaşanan yüksek coşkunun, paketi açıp ürünü eline aldıktan hemen sonra hızlıca sönüp gitmesi.\n" +
                    "Örnek 5: Sabahları yataktan kalkmak için hiçbir istek duymazken, yataktan kalkıp soğuk bir duş alıp hafif tempolu bir yürüyüş yaptıktan sonra günün geri kalanı için sisteminde uyanan o temiz ve dengeli hareket etme isteği.\n" +
                    "Örnek 6: Haftalardır üzerine çalıştığın zorlu bir yazılım projesini teslim etmeye sadece birkaç saat kalmışken hissettiğin o muazzam odaklanma ve bitirme hırsının, iş bittikten sonra yerini derin bir uyku ve dinginlik ihtiyacına bırakması.\n" +
                    "Başka örnekler de var. Bunlarla sınırlı düşünmeyin.";

            note = "Kişisel Not / Analiz\nZaten bilenler biliyor ama ben yine de söyleyeyim: En büyük hedef, sizin dopamin hormonlarınız. \n\n" +
                    "Burada hedeften kastım 'Bir suikastçı tuttular ve dopamin hormonlarınızı öldürecekler' falan değil. \n\n" +
                    "Neden size düzgün bir şekilde kısa videoları sunmak yerine o sonsuz aşağı doğru kaydırma (infinite scroll) mekanizmasını dayatıyorlar? Neden dijital dünyada her arayüz sizi durmaksızın kaydırmaya teşvik ediyor? \n\n" +
                    "İşte tam olarak bu yüzden; sistem beyninizi bedava ve eforsuz dopaminle köleleştirmek üzere kurulu. \n\n" +
                    "Burada size diyeceğim en önemli nokta şudur: Organik dopamin alın. \n\n" +
                    "Evet, farkındayım bazılarınız hemen 'Ne yani? Markete gidip meyve alır gibi dopamin mi alalım?' diyecek. Burada kastettiğim şey şu: Dopamini gidip faydalı ve efor gerektiren şeylerden alın. İmkanlarınızın el verdiği kadar, zihinsel veya fiziksel enerjinizi harcayan, arkasında bir emek barındıran faydalı eylemler yapın. \n\n" +
                    "Ve burada size 'Asla kısa video izlemeyin, telefonları çöpe atın' gibi tarikatvari, katı ve hayatsız bir yaklaşım uygulayın da demiyorum. Dengesini tutturun işte olduğu kadar. \n\n" +
                    "Sizin hayatınız bu.";

            dialogues = "dopamin örnek diyaloglar:\nDopamin Örnek Diyalog 1\nBağlam: Evde oturan iki arkadaş arasında geçen, sıradan bir motivasyon çöküşü anı.\n\n" +
                    "A: Bir şey yapasım yok. Şu koltukta bütün gün hiçbir şey yapmadan öylece oturabilirim.\n\n" +
                    "B: O derece mi boş hissediyorsun? Bir oyun ya da film açsam falan?\n\n" +
                    "A: Yok, o bile kurtarmıyor şu an.\n\n" +
                    "B: Sorunu çözdüm galiba.\n\n" +
                    "(A, meraklı gözlerle B'ye bakar)\n\n" +
                    "B: Kesin bir yargıda bulunamam ama net bir tahmin yürütebilirim. Bence senin dopamin havuzunun tabanı (baseline) şu an dibi görmüş durumda. Ya son günlerde beynini çok fazla bedava ve hızlı hazla (sosyal medya, abur cubur, aşırı oyun) yorup sistemi tükettin; ya da kafanı arkada çok büyük bir problem kurcalıyor ve beynin o sorunu çözmek için tüm enerjiyi (glikozu) harcarken, bu koltuktan kalkmanı sağlayacak o ödül/motivasyon kimyasalını sana lojistik olarak ateşlemiyor. Yani olay tembellik değil, dopamin lojistiğinin kesilmesi.\n\n" +
                    "A: Doğru gibi yani... Ben bunu bir düşüneyim.";

            importance = ""; // Dopamin için ayrı bir önem metni verilmediği için boş bırakıldı.

        } else {
            // Varsayılan Nöroplastisite Verisi
            title = "Nöroplastisite";
            desc = "Beynin deneyimlere bağlı olarak yapısını ve işlevini değiştirme yeteneğidir.";
            note = "benim kişisel geliştirici notum ve düşüncem o kavram hakkında (eğer varsa)";
            dialogues = "A: Beyin yaşlanınca değişmez mi?\nB: Hayır, nöroplastisite sayesinde her yaşta yeni bağlar kurulur.";
            importance = "Zihinsel becerileri geliştirmek için sürekli yeni şeyler öğrenmenin teorik dayanağıdır.";
        }

        // Metinleri ekrana bas
        if (txtTitle != null) txtTitle.setText(title);
        if (txtDesc != null) txtDesc.setText(desc);
        if (txtNote != null) txtNote.setText(note);
        if (txtDialogues != null) txtDialogues.setText(dialogues);
        if (txtImportance != null) txtImportance.setText(importance);

        // --- VERİ TABANI KONTROLÜ VE SON İNCELEME ZAMANINI İŞLEME ---
        List<ConceptItem_kavram> allConcepts = db.conceptDao_kavram().getAllConceptler();
        ConceptItem_kavram foundConcept = null;

        if (allConcepts != null) {
            for (ConceptItem_kavram item : allConcepts) {
                if (item.getTitle() != null && item.getTitle().equalsIgnoreCase(title)) {
                    foundConcept = item;
                    break;
                }
            }
        }

        if (foundConcept != null) {
            currentConcept = foundConcept;
        } else {
            currentConcept = new ConceptItem_kavram(title, desc, note, dialogues, importance, false);
            long newId = db.conceptDao_kavram().insert(currentConcept);
            currentConcept.setId((int) newId);
        }

        // KULLANICI SAYFAYA GİRDİĞİ AN İNCELEME ZAMANINI GÜNCELLİYORUZ
        if (currentConcept != null) {
            currentConcept.setLastViewedTime(System.currentTimeMillis());
            new Thread(() -> {
                db.conceptDao_kavram().update(currentConcept);
            }).start();
        }

        updateSaveButtonUI();
    }

    private void updateSaveButtonUI() {
        if (txtKaydet != null && currentConcept != null) {
            txtKaydet.setText(currentConcept.isSaved() ? "kaydedildi" : "kaydet");
        }
    }

    private void showFeedbackDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.hatali_bilgi_oneri_gonder, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        ImageView btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        EditText etFeedbackText = dialogView.findViewById(R.id.etFeedbackText);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSendFeedback = dialogView.findViewById(R.id.btnSendFeedback);

        if (btnCloseDialog != null) btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSendFeedback != null) {
            btnSendFeedback.setOnClickListener(v -> {
                String feedbackMessage = etFeedbackText.getText().toString().trim();

                if (feedbackMessage.isEmpty()) {
                    Toast.makeText(this, "Lütfen bir mesaj yazın", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(android.net.Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"destek@emailadresin.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Geri Bildirim");
                emailIntent.putExtra(Intent.EXTRA_TEXT, feedbackMessage);

                try {
                    startActivity(Intent.createChooser(emailIntent, "E-posta uygulamasını seçin:"));
                    dialog.dismiss();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "Cihazda e-posta uygulaması bulunamadı.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
    }

    private void showPopupMenu(View anchorView) {
        View popupView = getLayoutInflater().inflate(R.layout.kavram_sayfa_uc_nokta_menu, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(16f);

        LinearLayout menuCopyAll = popupView.findViewById(R.id.menuCopyAll);
        LinearLayout menuSharePage = popupView.findViewById(R.id.menuSharePage);
        LinearLayout menuReportError = popupView.findViewById(R.id.menuReportError);
        LinearLayout menuFontSize = popupView.findViewById(R.id.menuFontSize);

        if (menuCopyAll != null) {
            menuCopyAll.setOnClickListener(v -> {
                copyToClipboard("Tüm Sayfa", "Kavram detayları ve kişisel notlar...");
                popupWindow.dismiss();
            });
        }

        if (menuSharePage != null) {
            menuSharePage.setOnClickListener(v -> {
                shareText("Kavram Detayı", "Kavram detayları...");
                popupWindow.dismiss();
            });
        }

        if (menuReportError != null) {
            menuReportError.setOnClickListener(v -> {
                popupWindow.dismiss();
                showFeedbackDialog();
            });
        }

        if (menuFontSize != null) {
            menuFontSize.setOnClickListener(v -> {
                Toast.makeText(this, "Yazı boyutu ayarı açılacak", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });
        }

        popupWindow.showAsDropDown(anchorView, -100, 10);
    }
}
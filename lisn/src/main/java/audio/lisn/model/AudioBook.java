package audio.lisn.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

public class AudioBook implements Serializable{

    private static final long serialVersionUID = -7060210544600464481L;

    private String ISBN,book_id,duration,narrator,title, description, author, language, price, category,rate,
            cover_image, preview_audio,english_title;
    private String[] audio_file_urls;
    private boolean isPurchase;
    private int lastPlayFileIndex;
    private int lastSeekPoint;
    private int downloadCount;
    private float previewDuration;
    private boolean isDownloaded;
   // private boolean isPlayingPreview;
    //private String[] downloaded_file_urls;
    LanguageCode lanCode;
    private HashMap<String, String> downloadedFileList =null;

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

//    public boolean isPlayingPreview() {
//
//        return isPlayingPreview;
//    }
//
//    public void setPlayingPreview(boolean isPlayingPreview) {
//        this.isPlayingPreview = isPlayingPreview;
//    }

    public float getPreviewDuration() {
        return previewDuration;
    }

    public void setPreviewDuration(float previewDuration) {
        this.previewDuration = previewDuration;
    }


    public enum LanguageCode {
		LAN_EN, LAN_SI
	}
    public enum SelectedAction {
        ACTION_MORE, ACTION_PREVIEW,ACTION_DETAIL,ACTION_PURCHASE
    }

    public AudioBook() {
	}
    public AudioBook(JSONObject obj,int position) {
        String book_id="";
        try{
            book_id=obj.getString("book_id");
            if(obj.getString("author") !=null)
                this.author = obj.getString("author");
            if(obj.getString("cover_image") !=null)
                this.cover_image = obj.getString("cover_image");
            if(obj.getString("category") !=null)
                this.category = obj.getString("category");
            if(obj.getString("description") !=null)
                this.description = obj.getString("description");
            if(obj.getString("language") !=null)
                this.language = obj.getString("language");
            if(obj.getString("preview_audio") !=null)
                this.preview_audio = obj.getString("preview_audio");
            if(obj.getString("price") !=null)
                this.price = obj.getString("price");
            if(obj.getString("title") !=null)
                this.title = obj.getString("title");
            if(obj.getString("english_title") !=null)
                this.english_title = obj.getString("english_title");
            if(obj.getString("rate") !=null)
                this.rate = obj.getString("rate");
            if(obj.getString("duration") !=null)
                this.duration = obj.getString("duration");
            if(obj.getString("narrator") !=null)
                this.narrator = obj.getString("narrator");
            if(obj.getString("downloads") !=null)
                this.duration = obj.getString("downloads");
            JSONArray arr = obj.getJSONArray("audio_file");
            String[] list = new String[arr.length()];
            for(int index = 0; index< arr.length(); index++) {
                list[index] = arr.getString(index);
            }
            this.audio_file_urls=list;

        } catch (JSONException e) {
            book_id=""+position;
            e.printStackTrace();
        }
        this.book_id=book_id;
        this.ISBN=book_id;
        if (language.equalsIgnoreCase("si")) {
            this.lanCode = LanguageCode.LAN_SI;
        } else {
            this.lanCode = LanguageCode.LAN_EN;
        }


    }
	public AudioBook(String ISBN,String title,String english_title, String description, String author,
			String language, String price,String category, String cover_image, String preview_audio,String[] audio_file) {
		this.ISBN = ISBN;
		this.title = title;
		this.english_title=english_title;
		this.description = description;
		this.author = author;
		this.language = language;
		this.language = language;
		this.price = price;
		this.category = category;
		this.cover_image = cover_image;
		this.preview_audio = preview_audio;
		this.audio_file_urls =audio_file;

	}
    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNarrator() {
        return narrator;
    }

    public void setNarrator(String narrator) {
        this.narrator = narrator;
    }

    public int getLastPlayFileIndex() {
        return lastPlayFileIndex;
    }

    public void setLastPlayFileIndex(int lastPlayFileIndex) {
        this.lastPlayFileIndex = lastPlayFileIndex;
    }

    public int getLastSeekPoint() {
        return lastSeekPoint;
    }

    public void setLastSeekPoint(int lastSeekPoint) {
        this.lastSeekPoint = lastSeekPoint;
    }


    public String getISBN() {
		return ISBN;
	}

	public void setISBN(String iSBN) {
		ISBN = iSBN;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCover_image() {
		return cover_image;
	}

	public void setCover_image(String cover_image) {
		this.cover_image = cover_image;
	}

	public String getPreview_audio() {
		return preview_audio;
	}

	public void setPreview_audio(String preview_audio) {
		this.preview_audio = preview_audio;
	}


	public String getLanguage() {
		return language;
	}


	public void setLanguage(String language) {
		this.language = language;
		if (language.equalsIgnoreCase("si")) {
			this.lanCode = LanguageCode.LAN_SI;
		} else {
			this.lanCode = LanguageCode.LAN_EN;
		}
	}

	public LanguageCode getLanguageCode() {
		return lanCode;

	}

	public String getEnglish_title() {
		return english_title;
	}

	public void setEnglish_title(String english_title) {
		this.english_title = english_title;
	}

	public String[] getAudio_file_urls() {
		return audio_file_urls;
	}

    public boolean isPurchase() {
        return isPurchase;
    }

    public void setPurchase(boolean isPurchase) {
        this.isPurchase = isPurchase;
    }

    public void setAudio_file_urls(String[] audio_file_urls) {
		this.audio_file_urls = audio_file_urls;
	}
    public HashMap<String, String> getDownloadedFileList() {
        return downloadedFileList;
    }
    public void addFileToDownloadedList(String key,String url){

        if(downloadedFileList == null){
            downloadedFileList =new HashMap<String, String>();
        }
        downloadedFileList.put(key, url);

    }
    public void removeDownloadedFile(){
        if(downloadedFileList == null){
            downloadedFileList.clear();
        }
    }
    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

}
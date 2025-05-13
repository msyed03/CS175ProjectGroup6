from fastapi import FastAPI, UploadFile, File
import whisper,shutil,os,uuid
app=FastAPI()
model=whisper.load_model('medium')
@app.post("/transcribe")
async def transcribe_audio(file:UploadFile=File(...)):
    file_ext=file.filename.split(".")[-1]
    tempfilename=f"temp_{uuid.uuid4()}.{file_ext}"
    with open(tempfilename,'wb') as buffer:
        shutil.copyfileobj(file.file, buffer)
    result=model.transcribe(tempfilename)
    os.remove(tempfilename)
    return { "text": result["text"].trim() }

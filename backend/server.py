from fastapi import FastAPI, UploadFile, File
import whisper,shutil,os,uuid
import string
app=FastAPI()
model=whisper.load_model('base')
@app.post("/transcribe")
async def transcribe_audio(file:UploadFile=File(...)):
    file_ext=file.filename.split(".")[-1]
    tempfilename=f"temp_{uuid.uuid4()}.{file_ext}"
    with open(tempfilename,'wb') as buffer:
        shutil.copyfileobj(file.file, buffer)
    result=model.transcribe(tempfilename,language="es")
    os.remove(tempfilename)
    text= result["text"].strip()
    translator=str.maketrans('','',string.punctuation)
    cleaned=text.translate(translator)
    return { "text": cleaned }

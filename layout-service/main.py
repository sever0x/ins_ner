from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
from layout_extractor import extract_layout

app = FastAPI()

@app.post("/extract-layout")
async def extract_layout_endpoint(file: UploadFile = File(...)):
    if not file.filename.endswith(".pdf"):
        return JSONResponse({"error": "Only PDF supported"}, status_code=400)

    pdf_bytes = await file.read()
    layout = extract_layout(pdf_bytes)

    return JSONResponse({
        "filename": file.filename,
        "pages": layout
    })

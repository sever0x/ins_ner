import io
import pdfplumber

def extract_layout(pdf_bytes: bytes):
    result = []
    pdf_stream = io.BytesIO(pdf_bytes)
    with pdfplumber.open(pdf_stream) as pdf:
        for page_number, page in enumerate(pdf.pages, start=1):
            blocks = []
            for word in page.extract_words():
                blocks.append({
                    "text": word["text"],
                    "x0": word["x0"],
                    "y0": word["top"],
                    "x1": word["x1"],
                    "y1": word["bottom"]
                })

            result.append({
                "page_number": page_number,
                "width": page.width,
                "height": page.height,
                "blocks": blocks
            })
    return result

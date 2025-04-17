# 1. Base image
FROM python:3.9-slim

# 2. System deps for OpenCV, tkinter, build tools
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      build-essential \
      libgl1-mesa-glx \
      libglib2.0-0 \
      python3-tk && \
    rm -rf /var/lib/apt/lists/*

# 3. Workdir
WORKDIR /app

# 4. Copy & install Python deps
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. Copy code, model, DB (if desired)
COPY . .

# 6. (Optional) Initialize MySQL instead of SQLite
# RUN python setup_db.py

# 7. Launch your orchestrator
CMD ["python", "run_all.py"]

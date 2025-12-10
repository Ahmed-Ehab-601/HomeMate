#!/usr/bin/env python3
"""
One-time setup script to download and cache the toxic-comment-model.
Run this once before deploying the application to avoid model download during runtime.

Usage: python setup_toxic_model.py
"""
import os
import sys
from transformers import AutoTokenizer, AutoModelForSequenceClassification

# Use the same cache directory as the main script
CACHE_DIR = os.path.join(os.path.expanduser("~"), ".cache", "huggingface")
MODEL_NAME = "martin-ha/toxic-comment-model"

def download_model():
    """Download and cache the toxic-comment-model."""
    try:
        print(f"Downloading toxic-comment-model to cache directory: {CACHE_DIR}")
        print("This may take a few minutes (model is ~500MB)...")
        print("")
        
        # Download tokenizer
        print("→ Downloading tokenizer...")
        tokenizer = AutoTokenizer.from_pretrained(
            MODEL_NAME,
            cache_dir=CACHE_DIR
        )
        print("✓ Tokenizer downloaded successfully")
        
        # Download model
        print("→ Downloading model...")
        model = AutoModelForSequenceClassification.from_pretrained(
            MODEL_NAME,
            cache_dir=CACHE_DIR
        )
        print("✓ Model downloaded successfully")
        
        # Verify it works
        print("")
        print("→ Testing model with sample text...")
        test_text = "This is a test message"
        inputs = tokenizer(test_text, return_tensors="pt", truncation=True, max_length=512)
        outputs = model(**inputs)
        print("✓ Model is working correctly")
        
        print("")
        print("=" * 60)
        print("✓ Setup completed successfully!")
        print(f"✓ Model cached at: {CACHE_DIR}")
        print("✓ You can now run the application without model downloads")
        print("=" * 60)
        
        return True
        
    except Exception as e:
        print(f"✗ Error during setup: {str(e)}", file=sys.stderr)
        print("", file=sys.stderr)
        print("Possible solutions:", file=sys.stderr)
        print("1. Check your internet connection", file=sys.stderr)
        print("2. Ensure you have enough disk space (~500MB)", file=sys.stderr)
        print("3. Try running: pip install --upgrade transformers torch", file=sys.stderr)
        return False

if __name__ == "__main__":
    print("")
    print("=" * 60)
    print("Toxic-BERT Model Setup")
    print("=" * 60)
    print("")
    
    success = download_model()
    sys.exit(0 if success else 1)

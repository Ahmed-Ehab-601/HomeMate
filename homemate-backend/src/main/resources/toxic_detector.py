#!/usr/bin/env python3
"""
Toxic text detection script using martin-ha/toxic-comment-model.
Usage: python toxic_detector.py "text to check"
Returns: "true" if toxic (score > 0.5), "false" otherwise

This script uses HuggingFace's caching mechanism to avoid re-downloading the model.
Models are cached in: ~/.cache/huggingface/ (Linux/Mac) or C:\\Users\\<user>\\.cache\\huggingface\\ (Windows)
"""
import os
import sys
import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification

# Set cache directory explicitly (optional - transformers uses this by default)
CACHE_DIR = os.path.join(os.path.expanduser("~"), ".cache", "huggingface")
MODEL_NAME = "martin-ha/toxic-comment-model"

def is_toxic(text):
    """Check if text is toxic using toxic-bert model."""
    try:
        # Load model and tokenizer from cache ONLY (no downloading during runtime)
        # Model must be pre-cached using setup_toxic_model.py
        tokenizer = AutoTokenizer.from_pretrained(
            MODEL_NAME,
            cache_dir=CACHE_DIR,
            local_files_only=True  # Only use cached model, never download
        )
        model = AutoModelForSequenceClassification.from_pretrained(
            MODEL_NAME,
            cache_dir=CACHE_DIR,
            local_files_only=True  # Only use cached model, never download
        )
        
        # Set model to evaluation mode (faster inference)
        model.eval()
        
        # Disable gradient computation for inference (saves memory)
        with torch.no_grad():
            # Tokenize and get predictions
            inputs = tokenizer(text, return_tensors="pt", truncation=True, max_length=512)
            outputs = model(**inputs)
            scores = torch.softmax(outputs.logits, dim=1)
            
            # Get toxic score (index 1 is toxic class)
            toxic_score = scores[0][1].item()
        
        # Return true if toxic score > 0.5 threshold
        return toxic_score > 0.5
        
    except OSError as e:
        # Model not found in cache - need to run setup_toxic_model.py first
        print(f"Error: Model not found in cache. Please run 'python setup_toxic_model.py' first.", file=sys.stderr)
        print(f"Details: {str(e)}", file=sys.stderr)
        return False
    except Exception as e:
        # Other errors - fail-safe
        print(f"Error in toxicity detection: {str(e)}", file=sys.stderr)
        return False

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("false")
        sys.exit(0)
    
    text = sys.argv[1]
    result = is_toxic(text)
    print("true" if result else "false")

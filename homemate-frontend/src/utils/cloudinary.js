const CLOUDINARY_CLOUD_NAME = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;
const CLOUDINARY_API_KEY = import.meta.env.VITE_CLOUDINARY_API_KEY;
const CLOUDINARY_API_SECRET = import.meta.env.VITE_CLOUDINARY_API_SECRET;

const generateSignature = (paramsToSign, apiSecret) => {
  const sortedParams = Object.keys(paramsToSign)
    .sort()
    .map(key => `${key}=${paramsToSign[key]}`)
    .join('&');
  
  const signatureString = sortedParams + apiSecret;
  
  return crypto.subtle.digest('SHA-1', new TextEncoder().encode(signatureString))
    .then(hashBuffer => {
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    });
};

export const uploadToCloudinary = async (file, userId) => {
  const timestamp = Math.floor(Date.now() / 1000);
  const publicId = `${userId}_${timestamp}`;
  const folder = 'homemate/services';
  
  const paramsToSign = {
    folder: folder,
    public_id: publicId,
    timestamp: timestamp
  };
  
  const signature = await generateSignature(paramsToSign, CLOUDINARY_API_SECRET);
  
  const formData = new FormData();
  formData.append('file', file);
  formData.append('public_id', publicId);
  formData.append('folder', folder);
  formData.append('api_key', CLOUDINARY_API_KEY);
  formData.append('timestamp', timestamp);
  formData.append('signature', signature);

  try {
    const response = await fetch(
      `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`,
      {
        method: 'POST',
        body: formData,
      }
    );

    if (!response.ok) {
      const error = await response.json();
      console.error('Cloudinary upload error:', error);
      throw new Error(error.error?.message || 'Failed to upload image to Cloudinary');
    }

    const data = await response.json();
    return data.secure_url;
  } catch (error) {
    console.error('Upload error:', error);
    throw error;
  }
};

import {useState, useRef} from 'react';
import fileService from '../services/fileService.ts';

interface Props {
    currentImageUrl?: string | null;
    onImageUploaded: (url: string | null) => void;
}

export default function ImageUpload({ currentImageUrl, onImageUploaded }: Props) {
    const [preview, setPreview] = useState<string | null>(currentImageUrl || null);
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState('');
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if(!file)
            return;

        if(!file.type.startsWith('image/')) {
            setError('Please select an image.');
        }

        if(file.size > 10*1024*1024) {
            setError('Image must be smaller than 10MB.');
            return;
        }

        setError('');
        setUploading(true);

        const reader = new FileReader();
        reader.onload = () => setPreview(reader.result as string);
        reader.readAsDataURL(file);

        try {
            const url = await fileService.upload(file);
            onImageUploaded(url);
        }
        catch (err) {
            setError('Upload failed. Please try again.');
            setPreview(null);
            onImageUploaded(null);
        }
        finally {
            setUploading(false);
        }
    };

    const handleRemove = () => {
        setPreview(null);
        onImageUploaded(null);
        if(fileInputRef.current)
            fileInputRef.current.value = '';
    };

    return (
        <div className="mb-3">
            <label className="form-label">Image (optional)</label>

            {preview ? (
                <div className="position-relative mb-2">
                    <img
                        src={preview}
                        alt="Preview"
                        className="img-fluid rounded"
                        style={{ maxHeight: 200 }}
                    />
                    <button
                        type="button"
                        className="btn btn-sm btn-danger position-absolute top-0 end-0 m-1"
                        onClick={handleRemove}
                    >
                        ✕
                    </button>
                </div>
            ) : null}

            <input
                ref={fileInputRef}
                type="file"
                className="form-control"
                accept="image/*"
                onChange={handleFileChange}
                disabled={uploading}
            />

            {uploading && (
                <div className="mt-2">
                    <div className="spinner-border spinner-border-sm text-primary me-2" />
                    <small>Uploading...</small>
                </div>
            )}

            {error && <small className="text-danger">{error}</small>}
        </div>
    );
}
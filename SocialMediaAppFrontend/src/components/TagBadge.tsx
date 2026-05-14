import { Tag } from '../types';

interface Props {
    tag: Tag;
    onClick?: (tagName: string) => void;
}

export default function TagBadge({ tag, onClick }: Props) {
    return (
        <span
            className="badge bg-info bg-opacity-10 text-info me-1 tag-badge"
            onClick={() => onClick?.(tag.name)}
            style={{ cursor: onClick ? 'pointer' : 'default' }}
        >
      #{tag.name}
    </span>
    );
}
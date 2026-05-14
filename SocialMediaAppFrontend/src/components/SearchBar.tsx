import { useState } from 'react';
import * as React from "react";

interface Props {
    onSearch: (keyword: string) => void;
    onFilterByTag: (tag: string) => void;
    onFilterByUser: () => void;
    onClearFilters: () => void;
}

export default function SearchBar({ onSearch, onFilterByTag, onFilterByUser, onClearFilters }: Props) {
    const [searchText, setSearchText] = useState('');
    const [tagText, setTagText] = useState('');

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if(searchText.trim()) onSearch(searchText.trim());
    };

    const handleTagFilter = (e: React.FormEvent) => {
        e.preventDefault();
        if(tagText.trim()) onFilterByTag(tagText.trim());
    };

    return (
        <div className="card p-3 mb-3">
            <div className="row g-2">
                <div className="col-md-4">
                    <form onSubmit={handleSearch} className="input-group">
                        <input
                            type="text"
                            className="form-control form-control-sm"
                            placeholder="Search by title..."
                            value={searchText}
                            onChange={(e) => setSearchText(e.target.value)}
                        />
                        <button className="btn btn-outline-primary btn-sm" type="submit">Search</button>
                    </form>
                </div>
                <div className="col-md-4">
                    <form onSubmit={handleTagFilter} className="input-group">
                        <input
                            type="text"
                            className="form-control form-control-sm"
                            placeholder="Filter by tag..."
                            value={tagText}
                            onChange={(e) => setTagText(e.target.value)}
                        />
                        <button className="btn btn-outline-info btn-sm" type="submit">Filter</button>
                    </form>
                </div>
                <div className="col-md-4 d-flex gap-2">
                    <button onClick={onFilterByUser} className="btn btn-outline-secondary btn-sm flex-grow-1">
                        My Posts
                    </button>
                    <button onClick={onClearFilters} className="btn btn-outline-dark btn-sm flex-grow-1">
                        Clear
                    </button>
                </div>
            </div>
        </div>
    );
}
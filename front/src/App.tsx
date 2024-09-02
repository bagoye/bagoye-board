import React from 'react';
import './App.css';
import BoardItem from 'components/BoardItem';
import { commentListMock, favoriteListMock, latesBoardListMock, top3BoardListMock } from 'mocks';
import Top3Item from 'components/Top3Item';
import CommentItem from 'components/CommentItem';
import FavoriteItem from 'components/favoriteItem';

function App() {
  return (
    <>
      <div style={{padding: '0 20px', display: 'flex', flexDirection: 'column', columnGap: '30px', rowGap: '20px'}}>
          {favoriteListMock.map(favoriteListItem => <FavoriteItem favoriteListItem={favoriteListItem}/>)}
      </div>
      
      
    </>
  );
}

export default App;
